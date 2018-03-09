package com.xrtb;

import com.xrtb.bidder.ZPublisher;
import com.xrtb.jmq.EventIF;
import com.xrtb.jmq.MSubscriber;
import com.xrtb.shared.SharedObjectIF;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/** A class that sends messages to a swarm - used to make shared objects
 * Created by Ben M. Faul on 10/4/17.
 */
public class SharedRedissonObject implements EventIF {

    /** jackson object mapper */
    public static ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    /** The subscriber pool */
    MSubscriber pool;

    /** The handler to process the message */
    SharedObjectIF handler;

    /** Sends messages out to the rest of the swarm on an object update */
    ZPublisher publisher;

    /**
     * Create a server using 2 ports
     * @param handler ServerIF. The class handling the update.
     * @param hosts List. String list of members of the swarm.
     * @param port int. The 0MQ port for the subscriber.
     * @param port1 int. The 0MQ port for the publisher.
     * @throws Exception on network errors.
     */
    public SharedRedissonObject(SharedObjectIF handler, List<String> hosts, int port, int port1) throws Exception {
        init(handler, hosts, port, port1);
    }

    /**
     * Create a server using 1 port. Pub and sub on same channel. This is the normal mode
     * @param handler ServerIF. The class handling the update.
     * @param hosts List. String list of members of the swarm.
     * @param port int. The 0MQ port for the subscriber(local) and publishers(remote)
     * @throws Exception on network errors.
     */
    public SharedRedissonObject(SharedObjectIF handler, List<String> hosts, int port) throws Exception {
        init(handler, hosts, port, port);
    }

    /**
     * Initialize the publisher and subscriber.
     * @param handler ServerIF. The handler object when you get a message from a member of the swarm.
     * @param hosts List. A list of addresses in the swarm.
     * @param port int. The subscriber port
     * @param port1 int. The publisher port.
     * @throws Exception
     */
    void init(SharedObjectIF handler, List<String> hosts, int port, int port1) throws Exception {
        this.handler = handler;
        List<String> hs = new ArrayList();
        for (int i=0;i<hosts.size();i++) {
            String s = hosts.get(i);
            InetAddress address = InetAddress.getByName(s);

            // Don't add yourself, dummy
            if (address.getHostAddress().equals("127.0.0.1") == false) {
                s = "tcp://" + s + ":" + port + "&sro";
                hs.add(s);
            } else {
                if (port != port1) {
                    s = "tcp://" + s + ":" + port + "&sro";
                    hs.add(s);
                }
            }
        }

        pool = new MSubscriber(this,hs);
        pool.subscribe("sro");
        publisher = new ZPublisher("tcp://*:" + port1 + "sro");
    }

    public void transmit(String id, List list) {
        RedissonObjectMessage r = new RedissonObjectMessage();
        r.key = id;
        r.type = "List";
        r.type = "db";
        r.op = "add";
        r.value = list;
        String str = Tools.serialize(r);
        publisher.add(str);
    }

    public void transmit(String id, Map m) {
        RedissonObjectMessage r = new RedissonObjectMessage();
        r.key = id;
        r.type = "Map";
        r.type = "db";
        r.op = "add";
        r.value = m;
        String str = Tools.serialize(r);
        publisher.add(str);
    }

    public void transmit(String id, Map m, long timeout) {
        RedissonObjectMessage r = new RedissonObjectMessage();
        r.key = id;
        r.type = "Map";
        r.type = "cache";
        r.op = "add";
        r.value = m;
        r.timeout = timeout;
        String str = Tools.serialize(r);
        publisher.add(str);
    }

    public void transmit(String id, Set s) {
        RedissonObjectMessage r = new RedissonObjectMessage();
        r.key = id;
        r.type = "Set";
        r.type = "db";
        r.op = "add";
        r.value = s;
        String str = Tools.serialize(r);
        publisher.add(str);
    }

    public void transmit(String id, String s) {
        RedissonObjectMessage r = new RedissonObjectMessage();
        r.key = id;
        r.type = "String";
        r.type = "cache";
        r.op = "add";
        r.value = s;
        String str = Tools.serialize(r);
        publisher.add(str);
    }

    public void transmit(String id, String s, long timeout) {
        RedissonObjectMessage r = new RedissonObjectMessage();
        r.key = id;
        r.type = "String";
        r.type = "cache";
        r.op = "add";
        r.timeout = timeout;
        r.value = s;
        String str = Tools.serialize(r);
        publisher.add(str);
    }

    public void transmit(String id, AtomicLong v, long timeout) {
        RedissonObjectMessage r = new RedissonObjectMessage();
        Long value = v.get();
        r.key = id;
        r.type = "AtomicLong";
        r.type = "cache";
        r.op = "add";
        r.timeout = timeout;
        r.value = value;
        String str = Tools.serialize(r);
        publisher.add(str);
    }



    public void remove(String id) {
        RedissonObjectMessage r = new RedissonObjectMessage();
        r.key = id;
        r.type = "cache";
        r.op = "del";
        String str = Tools.serialize(r);
        publisher.add(str);
    }



    /**
     * Handle a message from the swarm, send it to the local object handler.
     * @param id String. The channel id.
     * @param msg String. The JSON marshalled message received.
     */
    @Override
    public void handleMessage(String id, String msg) {
        try {
            Object[] obj = Tools.deSerialize( msg);
            handler.handleMessage(obj[1]);
        } catch (Exception error) {
            System.out.println(error);
        }
    }

    /**
     * Got a 0MQ shutdown.
     */
    @Override
    public void shutdown() {
        pool.shutdown();
    }
}

