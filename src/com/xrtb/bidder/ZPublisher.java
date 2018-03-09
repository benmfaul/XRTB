package com.xrtb.bidder;

import com.xrtb.common.Configuration;
import com.xrtb.common.HttpPostGet;
import com.xrtb.jmq.*;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A publisher for ZeroMQ, File, and Logstash/http based messages, sharable by
 * multiple threads.
 *
 * @author Ben M. Faul
 */
public class ZPublisher implements Runnable, Callback {

    static final Logger clogger = LoggerFactory.getLogger(ZPublisher.class);
    // The objects thread
    protected Thread me;
    // The connection used
    String channel;
    // The topic of messages
    com.xrtb.jmq.Publisher logger;
    // The queue of messages
    protected ConcurrentLinkedQueue queue = new ConcurrentLinkedQueue();

    // Filename, if not using ZeroMQ
    protected String fileName;
    // The timestamp part of the name
    String tailstamp;
    // Logger time, how many minuutes before you clip the log
    protected int time;
    // count down time
    protected long countdown;
    // Strinbuilder for file ops
    volatile protected StringBuilder sb = new StringBuilder();
    // Object to JSON formatter
    protected ObjectMapper mapper;
    // Set if error occurs
    protected boolean errored = false;
    // Logging formatter yyyy-mm-dd-hh:ss part.
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm");

    ReentrantLock lockA = new ReentrantLock();

    Pinger ping;

    JedisPool jedisPool;

    // The kafka producer, when specifed
    Producer<String, String> producer;
    // The kafka topic
    String topic;
    int partition = 0;

    // Http endpoint
    HttpPostGet http;
    // Http url
    String url;
    // The time to buffer
    double total = 0;
    double count = 0;
    long errors = 0;
    double pe = 0;
    double bp = 0;
    double latency = 0;

    String address;

    /**
     * Default constructor
     */
    public ZPublisher() {

    }

    /**
     * A publisher that does ZeroMQ pub/sub
     *
     * @param address String. The zeromq topology.
     * @param topic   String. The topic to publish to.
     * @throws Exception
     */
    public ZPublisher(String address, String topic)  throws Exception {
        clogger.info("Setting zpublisher at: {} on topic: {}", address, topic);
        this.address = address;

        logger = new com.xrtb.jmq.Publisher(address, topic);
        me = new Thread(this);
        me.start();
    }

    /**
     * The HTTP Post, Zeromq, Redis and file logging constructor.
     *
     * @param address
     * String. Either http://... or file:// form for the loggert.
     * @throws Exception
     * on file IO errors.
     */
    static int k = 0;

    public ZPublisher(String address) throws Exception {

        if (address == null || clogger == null) // this can happen if some sub object is not configured by the top level logger
            return;

        this.address = address;
        clogger.info("Setting zpublisher at: {}", address);
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        if (address.startsWith("kafka://")) {
            doKafka(address);
        } else
        if (address.startsWith("file://")) {
            int i = address.indexOf("file://");
            if (i > -1) {
                address = address.substring(7);

                if (address.contains("$HOSTNAME")) {
                    address = Configuration.GetEnvironmentVariable(address,"$HOSTNAME",Configuration.instanceName);

                 /*   Map<String, String> env = System.getenv();
                    if (env.get("HOSTNAME") != null)
                        address = address.replace("$HOSTNAME", env.get("HOSTNAME"));
                    else
                        address = address.replace("$HOSTNAME", Configuration.instanceName); */
                }

                String[] parts = address.split("&");
                if (parts.length > 1) {
                    address = parts[0];
                    String[] x = parts[1].split("=");
                    time = Integer.parseInt(x[1]);
                    time *= 60000;
                    setTime();
                }
            }
            File f = new File(address);
            f.getParentFile().mkdirs();      // make any directories if they are needed

            this.fileName = address;
        } else if (address.startsWith("redis")) {
            String[] parts = address.split(":");
            channel = parts[1];
            //		jedisPool = Configuration.getInstance().jedisPool;                  TND FIX THIS
        } else if (address.startsWith("http")) {
            http = new HttpPostGet();
            int i = address.indexOf("&");
            if (i > -1) {
                address = address.substring(0, i);
                String[] parts = address.split("&");
                if (parts.length > 1) {
                    String[] x = parts[1].split("=");
                    time = Integer.parseInt(x[1]);
                }
            } else {
                url = address;
                time = 100;
            }
        } else {
            String[] parts = address.split("&");
            try {
                logger = new com.xrtb.jmq.Publisher(parts[0], parts[1]);
                topic = parts[1];
            } catch (Exception e) {
                clogger.error("Can't open 0MQ channel {}/{} because: {}", parts[0], parts[1], e.toString());
                throw e;
            }
        }
        me = new Thread(this);
        me.start();

        // If it's not a kafka thingie, then create a pinger.
        if (producer == null)
            ping = new Pinger(this);
    }

    // kafka://[a:b,b:c]&topic=bids&partition=0
    void doKafka(String saddress) throws Exception {
        KafkaConfig c = new KafkaConfig(saddress);
        Properties props = c.getProperties();
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);

        if (c.getTopic() == null)
            throw new Exception("Kafka publisher needs a topic: " + saddress);

        topic = c.getTopic();
        producer = new KafkaProducer<String, String>(props);
    }

    /**
     * Set the countdown timer when used for chopping off the current log and
     * making a new one.
     */
    void setTime() {
        countdown = System.currentTimeMillis() + time;
    }

    public Map getBp() {
        Map m = null;
        if (http == null)
            return null;

        if (errors != 0) {
            pe = 100 * errors / count;
        }
        if (count != 0) {
            bp = total / (count * this.time);
            latency = total / count;

        }

        m = new HashMap();
        m.put("url", url);
        m.put("latency", latency);
        m.put("wbp", bp);
        m.put("errors", errors);

        total = count = errors = 0;
        return m;
    }

    /**
     * Run the http post logger.
     */
    public void runHttpLogger() {
        Object obj = null;

        long elapsed = System.currentTimeMillis();
        String errorString = null;
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        while (true) {
            try {
                Thread.sleep(this.time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                if (sb.length() != 0) {
                    try {
                        if (lockA.tryLock(10, TimeUnit.SECONDS)) {
                            count++;
                            long time = System.currentTimeMillis();
                            http.sendPost(url, sb.toString());
                            int code = http.getResponseCode();
                            if (code == 200) {
                                time = System.currentTimeMillis() - time;
                                total += time;
                            } else {
                                errors++;
                            }
                        } else {
                            clogger.error("Erorr writing data to HTTP");
                        }

                    } catch (Exception error) {
                        // error.printStackTrace();
                        errorString = error.toString();
                        errors++;
                    }
                    sb.setLength(0);
                    sb.trimToSize();
                }
            } catch (Exception error) {
                errored = true;
                errors++;
                errorString = error.toString();
                // error.printStackTrace();
                sb.setLength(0);
            } finally {
                if (lockA.isHeldByCurrentThread()) lockA.unlock();
            }
        }
    }

    /**
     * Run the kafka logger in a loop
     */
    public void runKafkaLogger() {
        Object msg = null;
        String str = null;
        while (true) {
            try {
                while ((msg = queue.poll()) != null) {
                    if (ping != null)
                        ping.cancelPing();
                    str = Tools.serialize(mapper, msg);
                    ProducerRecord record =  new ProducerRecord<String, String>(topic, "key", str);
                    producer.send(record, this);
                }
                Thread.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
                // return;
            }
        }
    }

    /**
     * Is the queue empty.
     * @return boolean. Returns true if empty else false.
     */
    public boolean isQueueEmpty() {
        return queue.isEmpty();
    }

    /**
     * Run the file logger in a loop.
     */
    public void runFileLogger() {
        Object obj = null;

        String thisFile = this.fileName;

        if (countdown != 0) {
            tailstamp = "-" + sdf.format(new Date());
            thisFile += tailstamp;
        } else
            tailstamp = "";

        while (true) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (sb.length() != 0) {
                try {
                    if (lockA.tryLock(10, TimeUnit.SECONDS) == false) {
                        clogger.error("Error can'tobtain lock on write log for: {}", thisFile);
                    }
                    AppendToFile.item(thisFile, sb);
                } catch (Exception error) {
                    errored = true;
                    clogger.error("Publisher log error on {}: {}", fileName, error.toString());
                    error.printStackTrace();
                } finally {
                    sb.setLength(0);
                    sb.trimToSize();
                    if (lockA.isHeldByCurrentThread()) lockA.unlock();
                }
            }
            if (countdown != 0 && System.currentTimeMillis() > countdown) {
                thisFile = this.fileName + tailstamp;

                tailstamp = "-" + sdf.format(new Date());
                thisFile = this.fileName + tailstamp;
                setTime();
            }
        }
    }

    /**
     * The logger run method in a loop.
     */
    public void run() {
        try {
            if (producer != null)     // kafka
                runKafkaLogger();

            if (logger != null)       // jmq
                runJmqLogger();

            if (http != null)         // http
                runHttpLogger();

            if (jedisPool != null)    // redis
                runRedisLogger();

            runFileLogger();          // file
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    /**
     * Run the Redis logger in a loop.
     */
    public void runRedisLogger() throws Exception {
        Object msg = null;
        while (true) {
            try {
                while ((msg = queue.poll()) != null) {
                    if (ping != null)
                        ping.cancelPing();
                    jedisPool.getResource().publish(channel, msg.toString());
                }
                Thread.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
                // return;
            }
        }
    }

    /**
     * Run the ZeroMQ logger in a loop.
     */
    public void runJmqLogger() {
        Object msg = null;
        while (true) {
            try {
                while ((msg = queue.poll()) != null) {
                    if (ping != null)
                        ping.cancelPing();
                    logger.publish(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
                // return;
            }
        }
    }

    /**
     * Add a message to the messages queue.
     *
     * @param s . String. JSON formatted message.
     */
    public void add(Object s) {
        if (fileName != null || http != null) {
            if (errored)
                return;

            String contents = null;
            try {
                contents =  Tools.serialize(mapper, s);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                if (lockA.tryLock(10, TimeUnit.SECONDS)) {
                    sb.append(contents);
                    sb.append("\n");
                }
            } catch (Exception error) {
                error.printStackTrace();
            } finally {
                if (lockA.isHeldByCurrentThread()) lockA.unlock();
            }
        } else
            queue.add(s);
    }

    /**
     * Add a String to the messages queue without JSON'izing it.
     *
     * @param contents String. The string message to add.
     */
    public void addString(String contents) {
        if (producer != null) {
            ProducerRecord record =  new ProducerRecord<String, String>(topic, "key",contents);
            producer.send(record, this);
            return;
        }

        if (fileName != null || http != null) {
            try {
                if (lockA.tryLock(10, TimeUnit.SECONDS)) {
                    sb.append(contents);
                    sb.append("\n");
                } else {
                    clogger.error("Error, can't obtain lock for appending data to {}",fileName);
                }
            } catch (Exception error) {
                error.printStackTrace();
            } finally {
                if (lockA.isHeldByCurrentThread()) lockA.unlock();
            }
        } else
            queue.add(contents);
    }

    /**
     * Kafka callback
     * @param recordMetadata RecordMetaData. Information about the record that was sent
     * @param e Exception. If an exception was thrown, this will be non-null.
     */

    @Override
    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
        if (e != null) {
            clogger.error("Error while producing message to topic '{}', refer: {}: {}", topic, address, e.toString());
           if (e instanceof org.apache.kafka.common.errors.TimeoutException) {
               clogger.error("Restarting to try to recover");
               if (RTBServer.server != null)       // watch out don't do this if this is something besides the server.
                   RTBServer.panicStop();
               System.exit(1);
           }
        }
    }
}

class Pinger implements Runnable {
    Thread me = null;
    ZPublisher parent;
    volatile boolean cancel = true;

    public Pinger(ZPublisher parent) {
        this.parent = parent;
        me = new Thread(this);
        me.start();
    }

    public void run() {
        Ping ping = new Ping();
        while (true) {
            try {
                if (!cancel)
                    parent.add(ping);
                cancel = false;
                Thread.sleep(60000);
            } catch (Exception error) {
                error.printStackTrace();
            }

        }
    }

    public void cancelPing() {
        cancel = true;
    }

    public static void main(String [] args) throws Exception {
        String address = "kafka://[localhost:9092]&topic=bids";

        ZPublisher z = new ZPublisher(address);
        z.add("Hello world");
        Thread.sleep(2000);
    }
}