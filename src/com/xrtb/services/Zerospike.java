package com.xrtb.services;

/**
 * A replacement for Aerospike... 0Spike
 *
 * @Author Ben M. Faul
 */

import com.xrtb.bidder.ZPublisher;
import com.xrtb.common.Configuration;
import com.xrtb.tools.Performance;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.Utils;
import org.zeromq.ZMQ;
import org.zeromq.ZProxy;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Cross pub/sub with en event interface.
 */
public class Zerospike implements Runnable {

    protected static final Logger logger = LoggerFactory.getLogger(Zerospike.class);
    private volatile DB db;
    private HTreeMap objects;
    private ZPublisher kafkaLogger;

    ZMQ.Context context;
    ZMQ.Socket publisher;
    ZMQ.Socket subscriber;
    ZMQ.Socket listener = null;

    ListenToZeroMQ spy;

    /**
     * The JSON encoder/decoder object
     */
    protected static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private boolean trace = false;

    private String fileName;

    private volatile KeyDeletionScheduler worker;
    Thread me;

    /**
     * Run the program.
     * @param args String[]. -p -s are required. -p port -s port.
     * @throws Exception on parsing and 0mq errors.
     */
    public static void main(String[] args) throws Exception {
        int pub = 0;
        int sub = 0;
        int ct = 0;
        int listen = 0;
        boolean trace = false;
        String strace = null;
        String db = null;
        String kafka = "kafka://[$BROKERLIST]&topic=status";
        int i = 0;
        while (i < args.length) {
            switch (args[i]) {
                case "-p":
                    pub = Integer.parseInt(args[i + 1]);
                    i += 2;
                    break;
                case "-s":
                    sub = Integer.parseInt(args[i + 1]);
                    i += 2;
                    break;
                case "-ct":
                    ct = Integer.parseInt(args[i + 1]);
                    i+= 2;
                    break;
                case "-t":
                    trace = true;
                    i++;
                    break;
                case "-f":
                    db = args[i + 1];
                    i += 2;
                    break;
                case "-k":
                    kafka = args[i + 1];
                    i += 2;
                    break;
                case "-i":
                    listen = Integer.parseInt(args[i + 1]);
                    i += 2;
                    break;
                case "-h":
                    System.out.println("Usage: -p pubport -s subport -k kafka[localhost:9092] -i initport [-t]");
                    return;
                default:
                    System.out.println("Unknown param: " + args[i]);
                    return;
            }

        }

        kafka = Configuration.substitute(kafka);

        if (ct == 0) {
            String value = Configuration.GetEnvironmentVariable("$THREADS", "$THREADS", "1");
            ct = Integer.parseInt(value);
        }

        if (pub == 0) {
            String value = Configuration.GetEnvironmentVariable("$PUBPORT", "$PUBPORT", "6001");
            pub = Integer.parseInt(value);
        }

        if (sub == 0) {
            String value = Configuration.GetEnvironmentVariable("$SUBPORT", "$SUBPORT", "6000");
            sub = Integer.parseInt(value);
        }
        if (listen == 0) {
            String value = Configuration.GetEnvironmentVariable("$INITPORT", "$INITPORT", "6002");
            listen = Integer.parseInt(value);
        }

        if (db == null) {
            db = Configuration.GetEnvironmentVariable("$CACHE", "$CACHE", "cache.db");
        }

        if (strace == null) {
            String value = Configuration.GetEnvironmentVariable("$TRACE", "$TRACE", "false");
            trace = Boolean.parseBoolean(value);
        }

        Zerospike spike = new Zerospike(sub, pub, listen, db, kafka, trace, ct);

       //RTopic bids = new RTopic("tcp://localhost:6001&context");
       //bids.addListener(new MessageListener<Object>() {
       //     @Override    public void onMessage(String channel, Object br) {
       //        System.out.println("<<<<<<<<<<<<<<<<<" + br); }});

       // Publisher s = new Publisher("tcp://localhost:6000", "bids");
        //s.publish("Hello World");

        logger.info("Zerospike service started, publish: {}, subscribe: {}, db: {}, context threads: {}", pub, sub, db, ct);
        while (true) {
            try {
                //s.publish("Hello World");
                Thread.sleep(5000);
                //        logger.info("Zerospike service: processed {} msgs in last minute",spike.getClearCount());
            } catch (Exception error) {
                error.printStackTrace();
                break;
            }
        }
        logger.error("Zerospike was interrupted!, it will now exit");
        System.exit(0);
    }

    /**
     * Create the Cross pub/sub service.
     * @param pub int. The publisher port.
     * @param sub int. The subscriber port.
     * @throws Exception on 0MQ errors.
     */
    public Zerospike(int sub, int pub, int listen, String fileName, String kafka, boolean trace, int ct) throws Exception {

        setInstance();

        context = ZMQ.context(ct);

        subscriber = context.socket(ZMQ.XSUB);
        subscriber.bind("tcp://*:" + sub);
        subscriber.setHWM(1000000);

        publisher = context.socket(ZMQ.XPUB);
        publisher.bind("tcp://*:" + pub);
        publisher.setHWM(1000000);

        // Uncomment this to receive info from the connections
        int anyPort = Utils.findOpenPort();
        ;
        listener = context.socket(ZMQ.PAIR);
        listener.connect("tcp://localhost:" + anyPort);
        listener.setHWM(1000000);

        this.fileName = fileName;
        this.trace = trace;
        try {
            if (kafka != null) {
                kafkaLogger = new ZPublisher(kafka);
            }
            db = DBMaker.fileDB(fileName).transactionEnable().make();
            objects = db.hashMap("objects").createOrOpen();
        } catch (Exception error) {
            error.printStackTrace();
            System.exit(1);
        }

        if (objects == null) {
            objects = db.hashMap("objects").create();
            logger.warn("Had to create initial objects.");
        }

        worker = new KeyDeletionScheduler(objects, logger);
        worker.setTrace(trace);

        spy = new ListenToZeroMQ(anyPort, objects, worker, logger, trace);

        initializeLoad();

        AddShutdownHook hook = new AddShutdownHook();
        hook.attachShutDownHook(this);

        ScheduledExecutorService execService = Executors.newScheduledThreadPool(5);
        execService.scheduleAtFixedRate(() -> {
            try {
                printStatus();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }, 1, 1, TimeUnit.MINUTES);
        logger.info("*** System starting: publisher is at {}, subscriber is at {}, xfr is at {}", pub, sub, listen);
        if (kafkaLogger != null) {
            kafkaLogger.add("System starting: publisher is at " + pub + " subscriber is at " + sub);
        }
        new FileServer(logger, objects, listen);
        me = new Thread(this);
        me.start();
    }

    public void run() {
        ZProxy.Proxy proxy;
        try {
            ZMQ.proxy(subscriber, publisher, listener, null);
        } catch (Exception error) {
            error.printStackTrace();
        }
    }


    /**
     * Set the instance name if not already set.
     */
    private void setInstance() {
        if (Configuration.instanceName.equals("default") == false)
            return;

        java.net.InetAddress localMachine = null;
        String useName = null;
        String ipAddress = null;
        try {
            localMachine = java.net.InetAddress.getLocalHost();
            ipAddress = localMachine.getHostAddress();
            useName = localMachine.getHostName();
        } catch (Exception error) {
            useName = Configuration.getIpAddress();
        }

        Configuration.instanceName = useName;
    }

    /**
     * Load the expirer up from the database.
     */
    private void initializeLoad() {
        long now = System.currentTimeMillis();
        Set elements = objects.entrySet();
        logger.info("Initialization starts with {} elements", elements.size());
        int count = 0;

        for (Object e : elements) {
            HTreeMap.Entry xx = (HTreeMap.Entry) e;
            String key = (String) xx.getKey();
            Map x = (Map) xx.getValue();
            Number time = (Number) x.get("expire");
            if (now > time.longValue()) {
                objects.remove(key);
                count++;
            } else {
                worker.addKey(key, time.longValue());
            }
            ;
        }
        logger.info("Initialization complete, {} keys expired, {} are left.", count, objects.size());
        if (kafkaLogger != null) {
            kafkaLogger.add(String.format("Initialization complete, %d keys expired, %d are left.", count, objects.size()));
        }
    }

    /**
     * Set the trace flag. Set t to true to view the data coming in.
     * @param t boolean. True means trace, false means don't trace
     */
    public void setTrace(boolean t) {
        trace = t;
        worker.setTrace(t);
    }

    /**
     * Print the performance over the last minute
     */
    private void printStatus() {
        db.commit();
        int elementCount = objects.getSize();
        logger.info("Threads: {}, CPU: {}, Memory: {}, Hits: {}, elements: {}", Performance.getThreadCount(),Performance.getCpuPerfAsString(),Performance.getMemoryUsed(), spy.getClearCount(), elementCount);
    }

    public void clear() {
        objects.clear();
    }

    public int getSize() {
        return objects.getSize();
    }

    protected void panicStop() {
        shutdown();
        logger.warn("System has shutdown");
    }

    /**
     * We are stopping now.
     */
    public void shutdown() {
        db.commit();
        db.close();
    }
}


class AddShutdownHook {
    Zerospike cache;

    public void attachShutDownHook(final Zerospike cache) {
        this.cache = cache;
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                cache.panicStop();
            }
        });
        cache.logger.info("*** Shut Down Hook Attached. ***");
    }
}

