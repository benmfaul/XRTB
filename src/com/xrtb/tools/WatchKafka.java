package com.xrtb.tools;

import com.xrtb.common.Configuration;

/**
 * Created by ben on 12/23/17.
 */
public class WatchKafka {
    int count = 0;

    public static void main(String [] args) throws Exception {

        String address = "kafka://[$BROKERLIST]&topic=requests&groupid=reader";

        if (args.length > 0)
            address = args[0];

        address = Configuration.substitute(address);

        System.out.println("WatchKafka starting: " + address);
        new WatchKafka(address);
    }

    public WatchKafka(String address)throws Exception  {
        com.xrtb.jmq.RTopic channel = new com.xrtb.jmq.RTopic(address);
        channel.addListener(new com.xrtb.jmq.MessageListener<Object>() {
            @Override
            public void onMessage(String channel, Object data) {
                System.out.println(channel + " [" + count + "] = " + data + "\n");
                count++;
            }
        });

    }
}
