package com.xrtb.shared;

import com.xrtb.pojo.BidRequest;
import com.xrtb.services.Zerospike;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A class that implements a frequency governer. The first concept is, we have a SelfExpiringHashSet that adds a key
 * to the Set. This key will expire in 1 second. The next bid that comes in: If the key still exists, then we no bid
 * on that campaign; if the key is no longer present it can be considered for a bid.
 * <p>
 * When a key is added, the class will transmit the key to any subscribers to this 0MQ port. Thus multiple bidders can
 * share the keys across them. So if a user gets a bid, then all other bidders know about it. Then if within 1 second a
 * bid comes in and this key matches, the second bidder will not bid on it.
 * <p>
 * Created by Ben M. Faul on 10/4/17.
 */
public class FrequencyGoverner implements SharedObjectIF<String> {

    public static final String KEY = "freqgov";
    /**
     * The self expiring hash set
     */
    private volatile FreqSetCache eset = new FreqSetCache(); // = new SelfExpiringHashSet();

    /**
     * A shared object handler for this object. Traansmits keys that we set to members of the swarm
     */
    private SharedObject server;

    /**
     * Use this flag to turn off the contains() so it always returns false. Good for testing.
     */
    public static AtomicBoolean silent = new AtomicBoolean(false);

    /**
     * Useful for debugging
     */
    public String name;

    /**
     * Object timeout in milli seconds
     */
    long timeout = 900;

    /**
     * Set a frequency governer using a list of bidders, and on  specified porta. Used for debugging on the same 0MQ system.
     *
     * @param address String. The host address.
     * @param port      int. The port to use for subscribing.
     * @param port1     int. The port to use for publishing
     * @param timeout   long. The timeout to use in ms., if the default is not used.
     * @throws Exception on 0MQ errors.
     */
    public FrequencyGoverner(String address, int port, int port1, long timeout) throws Exception {
        server = new SharedObject(this, address, port, port1,KEY);
        this.timeout = timeout;
    }

    /**
     * Return the subscription binding for this service.
     * @return String. The binding for subscribers.
     */
    public String getSubscriptionBinding() {
        return server.subscriptionBinding();
    }

    /**
     * Return the publisher binding for this service.
     * @return String. The binding for publisher.
     */
    public String getPublisherBinding() {
        return server.publisherBinding();
    }

    /**
     * Add a string value to the Set.
     *
     * @param id String. The value to add.
     */
    public void add(String id) {
        eset.add(id, timeout);
        server.transmit(id);
    }

    /**
     * Add a string value to the set, make a key from the campaign + the bid request synthkey
     *
     * @param camp String. The campaign id.
     * @param br   BidRequest. The bid request providing the synthkey
     */
    public void add(String camp, BidRequest br) {
        StringBuilder sb = new StringBuilder(camp);
        sb.append(":");
        sb.append(br.synthkey);
        String key = sb.toString();

        add(key);
    }

    /**
     * Does the Set contain a key based on campaign id + br.synthkey.
     *
     * @param adId String. The campaign ad it.
     * @param br   BidRequest. The bid request providing the synthkey
     * @return boolean. Returns true if the Set still has the value. Otherwise return false.
     */
    public boolean contains(String adId, BidRequest br) {
        StringBuilder sb = new StringBuilder(adId);
        sb.append(":");
        sb.append(br.synthkey);
        String key = sb.toString();
        return contains(key);
    }

    /**
     * Return whether a string key exists.
     *
     * @param id String. The value of the key.
     * @return boolean. Returns true if the key still exists, or false if it ages out.
     */
    public boolean contains(String id) {

        return eset.contains(id);
    }

    public void clear() {
        eset.clear();
    }

    /**
     * Handle a message from 0MQ. A member of the swarm of bidders is setting a key.
     *
     * @param ip String. The key to set.
     */
    @Override
    public void handleMessage(String ip) {
        eset.add(ip, timeout);
    }

    /**
     * Shut the system down
     */
    public void close() {
        server.shutdown();
    }

    /**
     * A simple test progra, to demonstrate how the 0MQ
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        String address = "localhost";

        new Zerospike(6000, 6001, 6002, "cache.db", "http://[localhost:9092]", true, 1);


        Thread.sleep(1000);

        FrequencyGoverner fg1 = new FrequencyGoverner(address, 6001, 6000, 900);
        fg1.name = "FG1";
        FrequencyGoverner fg2 = new FrequencyGoverner(address, 6001, 6000, 900);
        fg2.name = "FG2";

        Thread.sleep(1000);

        fg1.add("hello");
        Thread.sleep(100);
        System.out.println(fg2.contains("hello"));
        Thread.sleep(100);
        System.out.println(fg2.contains("hello"));

    }

}
