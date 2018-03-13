package com.xrtb.services;

import org.mapdb.HTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A listener class to the 0MQ XPUB/XSUB broker. This guy listens to topic "context". If it heres it, it writes the information
 * to disk. This is what keeps lasting data like frequency caps. Everything else is cache based and is just ignored.
 */
class ListenToZeroMQ implements Runnable {

    /** My thread */
    private Thread me;
    /** The pipe for connecting to the XPS broker */
    private ZMQ.Socket pipe;
    /** Counter for the number of times we have written to disk in the last minute */
    private AtomicLong count = new AtomicLong(0);
    /** Counter for total hits */
    private AtomicLong totalCount = new AtomicLong(0);
    /** Traces the data through the system */
    private boolean trace;
    /** The logger for stats and logs */
    protected static final Logger logger = LoggerFactory.getLogger(ListenToZeroMQ.class);
    /** The worker that is used to delete stuff out of the database file */
    private KeyDeletionScheduler worker;
    /** The object that is mapped to disk */
    private HTreeMap objects;

    /**
     * Constructor for the spy listening to context topic
     * @param anyPort int. THe port used to connect to the broker.
     * @param objects HTreeMap. The mapping to the file on disk.
     * @param worker KeyDeletionScheduler. The worker responsible for deleting data on disk when the key expires.
     * @param trace boolean. Set to true to watch activity on the context topic.
     */
    public ListenToZeroMQ(int anyPort, HTreeMap objects, KeyDeletionScheduler worker, boolean trace) {
        this.objects = objects;
        this.worker = worker;
        this.trace = trace;
        ZMQ.Context context = ZMQ.context(1);
        pipe = context.socket(ZMQ.PAIR);
        pipe.bind("tcp://*:"+anyPort);

        me = new Thread(this);
        me.start();
    }

    /**
     * The runnable that handles context information. As the context topic data is routed to subscribers, this guy listens in
     * and copies relevant stuff to disk.
     */
    public void run() {
        while (!me.isInterrupted()) {
            // Read envelope with address
            String topic = pipe.recvStr();
            //System.out.println("---------------->" + topic);
            if (topic.charAt(0)!=1) {
                String msg = pipe.recvStr();
                if (trace)
                    logger.info("Cache RECV, topic: '{}', msg: '{}'", topic, msg);
                if (topic.equals("context")) {
                    if (msg.length() > 16) {
                        try {
                            if (!msg.endsWith("com.c1x.bidder3.rtb.jmq.Ping\"}")) {
                                count.incrementAndGet();
                                Map value = Zerospike.mapper.readValue(msg, Map.class);
                                handleContext(value);
                            }
                        } catch (Exception error) {
                            error.printStackTrace();
                            System.out.println("TOPIC:" + topic);
                            System.out.println("MSG: " + msg);
                            logger.error("Zeromq interrupted: {}" + error.toString());
                        }
                    }
                }


                totalCount.addAndGet(1);
                //System.out.println("I spy " + contents + " on " + address);
            }
        }
        logger.error("Zeromq listener has exited");
    }

    /**
     * Get the count of the times we have received a disk based message, then zero the count.
     * @return long. The number of times we have encountered a message.
     */
    public long getClearCount() {
        return count.getAndSet(0);
    }

    /**
     * Get the count of the times we have received a message, then zero the count.
     * @return long. The number of times we have encountered a message.
     */
    public long getTotalClearCount() {
        return totalCount.getAndSet(0);
    }

    /**
     * Handle the context messages. We will keep a copy of the object in the file database. We will
     * keep the key and the timeout in a delayed queue. We will then add or delete objects as directed.
     * @param m Map. The data being stored to disk.
     */
    private void handleContext(Map m) {
        String cmd = (String) m.get("command");
        if (cmd == null)
            return;
        String key = (String) m.get("key");
        Number expire = (Number) m.get("expire");
        switch (cmd) {
            case "hmset":
                if (expire.longValue()<=0) {
                    logger.error("Hmset {} has an invalid timeout: {}",key,expire);
                    return;
                }
                objects.put(key, m);
                worker.addKey(key, expire.longValue());
                break;
            case "set":
                if (expire.longValue()<=0) {
                    logger.error("Set {} has an invalid timeout: {}",key,expire);
                    return;
                }
                objects.put(key, m);
                worker.addKey(key, expire.longValue());
                break;
            case "del":
                objects.remove(key);
                worker.delKey(key);
                break;
            case "incr":
                /**
                 * Override the -1 in an expire for timeout.
                 */
                Long ttl = null;
                if (expire == null || expire.longValue()<=0) {
                    ttl = worker.getTTL(key);
                    if (ttl != null)
                        m.put("expire",ttl.longValue());
                    else {
                        logger.error("Incrementing {} caused an error, expire: {}, derived ttl: {}",key,expire,ttl);
                        return;
                    }
                } else
                    ttl = expire.longValue();
                objects.put(key, m);
                worker.addKey(key, ttl);
                break;
            default:
                logger.error("Unknown distributed cache command: {}, key: {}", cmd, key);

        }
    }
}