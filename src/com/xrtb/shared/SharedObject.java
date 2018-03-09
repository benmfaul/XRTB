package com.xrtb.shared;

import com.xrtb.bidder.ZPublisher;
import com.xrtb.jmq.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/** A class that sends messages to a swarm - used to make shared objects
 * Created by Ben M. Faul on 10/4/17.
 */
public class SharedObject implements EventIF {

    String KEY;

    /** jackson object mapper */
    public static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    /** The subscriber */
    MSubscriber subscription;

    /** The publisher */
    ZPublisher publisher;

    /** The handler to process the message */
    SharedObjectIF handler;

    /** The binding string for the publisher */
    String publisherBinding;

    /** The binding string for the subscription */
    String subscriptionBinding;

    /**
     * Initialize the publisher and subscriber.
     * @param handler ServerIF. The handler object when you get a message from a member of the swarm.
     * @param host String. The hostname of the freq goberner service.
     * @param port int. The subscriber port
     * @param port1 int. The publisher port.
     * @throws Exception
     */
    public SharedObject(SharedObjectIF handler, String host, int port, int port1, String key) throws Exception {
        this.KEY = key;
        this.handler = handler;

        publisherBinding = "tcp://"+host+":"+port1;
        subscriptionBinding = "tcp://"+host+":"+port;

        subscription = new MSubscriber(this,subscriptionBinding,KEY);

        publisher = new ZPublisher(publisherBinding,KEY);        // don't use publisher, this looks like a context issue
    }

    /**
     * Send an object to the subscribers.
     * @param obj Object. The object to send. Serialized as JSON.
     */
    public void transmit(Object obj) {
        String str = Tools.serialize(mapper,obj);
        publisher.add(str);
    }

    /**
     * Returns the publisher binding
     * @return String. The binding for this punlisher
     */
    public String publisherBinding() {
        return publisherBinding;
    }

    public String subscriptionBinding() {
        return subscriptionBinding;
    }


    /**
     * Handle a message from the swarm, send it to the local object handler.
     * @param id String. The channel id.
     * @param msg String. The JSON marshalled message received.
     */
    @Override
    public void handleMessage(String id, String msg) {
        try {
            Object[] obj = Tools.deSerialize(mapper, msg);
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
        // nothing to do
    }
}

