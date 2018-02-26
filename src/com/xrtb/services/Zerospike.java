package com.xrtb.services;

/**
 * A class that provides a Zeromq broker for use with RTB4FREE, this eliminates the need for a star pattern in the controller for
 * commanding the bidders.
 */

import com.xrtb.common.Configuration;
import com.xrtb.jmq.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.Utils;
import org.zeromq.ZMQ;
import org.zeromq.ZProxy;

import java.util.concurrent.atomic.AtomicLong;

public class Zerospike implements Runnable {

    static final Logger logger = LoggerFactory.getLogger(Zerospike.class);
    ZMQ.Context context;
    ZMQ.Socket publisher;
    ZMQ.Socket subscriber;
    ZMQ.Socket listener = null;
    ListenToZeroMQ spy;
    Thread me;

    public static void main(String args[]) throws Exception {
        int i = 0;
        String sub = Configuration.GetEnvironmentVariable("$SUBPORT","$SUBPORT","6000");
        String pub = Configuration.GetEnvironmentVariable("$PUBPORT","$PUBPORT","6001");

        while(i < args.length) {
            switch(args[i]) {
                case "-p":
                    pub = args[i+1];
                    i+=2;
                    break;
                case "-s":
                    sub = args[i+1];
                    i+=2;
                    break;
                case "-h":
                    System.out.println("com.xrtb.services.Zerospike [-p portnum] [-s portnum]\n" +
                                        "\twhere -p is the publisher port and -s is the subscriber port\n" +
                                        "\tYou can also use environment variables SUBPORT and PUBPORT too");
            }
        }
     // Zerospike spike = new Zerospike(Integer.parseInt(sub),Integer.parseInt(pub));

        RTopic bids = new RTopic("tcp://localhost:6001&bids");
        bids.addListener(new MessageListener<Object>() {
            @Override
            public void onMessage(String channel, Object br) {
                System.out.println("<<<<<<<<<<<<<<<<<" + br);
            }
        });

        Publisher s = new Publisher("tcp://localhost:6000", "bids");
        s.publish("Hello World");

        logger.info("Zerospike service started, publish: {}, subscribe: {}", pub, sub);
        while(true) {
            try {
                s.publish("Hello World");
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

    public Zerospike(int sub, int pub) throws Exception {

        context = ZMQ.context(1);

        subscriber = context.socket(ZMQ.XSUB);
        subscriber.bind("tcp://*:"+sub);

        publisher = context.socket(ZMQ.XPUB);
        publisher.bind("tcp://*:"+pub);

        // Uncomment this to receive info from the connections
        int anyPort = Utils.findOpenPort();;
        listener = context.socket(ZMQ.PAIR);
        listener.connect("tcp://localhost:"+anyPort);

        spy = new ListenToZeroMQ(anyPort);

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

    public long getClearCount() {
        return spy.getClearCount();
    }
}

class ListenToZeroMQ implements Runnable {

    Thread me;
    ZMQ.Socket pipe;
    ZMQ.Context context;
    AtomicLong count = new AtomicLong(0);
    public ListenToZeroMQ(int anyPort) {
        context = ZMQ.context(1);
        pipe = context.socket(ZMQ.PAIR);
        pipe.bind("tcp://*:"+anyPort);

        me = new Thread(this);
        me.start();
    }

    public void run() {
        while (me.isInterrupted()==false) {
            // Read envelope with address
            String address = pipe.recvStr();
            String contents = pipe.recvStr();
            count.addAndGet(1);
            //System.out.println("I spy " + contents + " on " + address);
        }
    }

    public long getClearCount() {
        long x = count.getAndSet(0);
        return x;
    }


}