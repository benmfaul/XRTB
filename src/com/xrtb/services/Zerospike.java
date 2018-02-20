package com.xrtb.services;

import com.xrtb.jmq.*;
import com.xrtb.pojo.BidResponse;
import org.zeromq.ZMQ;
import org.zeromq.ZProxy;

public class Zerospike implements Runnable {

    ZMQ.Context context;
    ZMQ.Socket publisher;
    ZMQ.Socket subscriber;
    ZMQ.Socket listener;
    Thread me;

    public static void main(String args[]) throws Exception {
        int i = 0;
        int sub = 6000;
        int pub = 6001;

        while(i < args.length) {
            switch(args[i]) {
                case "-p":
                    break;
                case "-s":
                    break;
            }
        }
        new Zerospike(sub,pub);

        RTopic bids = new RTopic("tcp://localhost:6001&bids");
        bids.addListener(new MessageListener<String>() {
            @Override
            public void onMessage(String channel, String br) {
                System.out.println("<<<<<<<<<<<<<<<<<" + br);
            }
        });

        Publisher s = new Publisher("tcp://localhost:6000", "bids");
        s.publish("Hello World");

        Thread.sleep(100000);
    }

    public Zerospike(int sub, int pub) {

        context = ZMQ.context(1);

        subscriber = context.socket(ZMQ.XSUB);
        subscriber.bind("tcp://*:"+sub);

        publisher = context.socket(ZMQ.XPUB);
        publisher.bind("tcp://*:"+pub);

        listener = context.socket(ZMQ.PAIR);
        listener.connect("inproc://pipe");

        me = new Thread(this);
        me.start();
    }

    public void run() {
        ZProxy.Proxy proxy;
        try {
            ZMQ.proxy(subscriber, publisher, null, null);
        } catch (Exception error) {
            error.printStackTrace();
        }
    }
}