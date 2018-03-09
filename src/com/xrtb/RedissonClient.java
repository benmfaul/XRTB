package com.xrtb;


import com.xrtb.bidder.RTBServer;
import com.xrtb.bidder.ZPublisher;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.jmq.EventIF;
import com.xrtb.jmq.MSubscriber;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.net.Socket;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A Replacement for the Redisson object. This class is a serialized (JSON) interface to the Aerospike/Cache2k database.
 *
 * @author Ben M. Faul
 */
public class RedissonClient implements EventIF {

    protected static final Logger logger = LoggerFactory.getLogger(RedissonClient.class);

    /**
     * The system clock
     **/
    public static Clock clock = Clock.systemUTC();

    /**
     * If aerospike is not used, the cache (bids) database in cache2k form
     */
     volatile Cache cache;
    /**
     * If aerospike is not used, the cache database of the User and Blacklist object
     */
     volatile Cache cacheDb;

     Reader reader;

    /**
     * Used to communicate the cache updates to sibling bidders (but not cacheDb, which is local
     */
    ZPublisher freq;
    MSubscriber sfreq;

    String self = UUID.randomUUID().toString();

    /**
     * The JSON encoder/decoder object
     */
    public static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Instantiate the Redisson object using the Cache2k systen (embedded cache, single server system).
     */
    public RedissonClient() {
        cache = new Cache2kBuilder<String, Object>() {
        }.expireAfterWrite(300, TimeUnit.SECONDS).build();
        cacheDb = new Cache2kBuilder<String, Object>() {
        }.build();
        cacheDb.put("users-database", new ArrayList<Campaign>());
    }

    public void setSharedObject(String host, int port) throws Exception {
        if (RTBServer.frequencyGoverner != null) {
            String pub = RTBServer.frequencyGoverner.getPublisherBinding();
            pub += "&context";
            freq = new ZPublisher(pub);

            String sub = RTBServer.frequencyGoverner.getSubscriptionBinding();
            sfreq = new MSubscriber(this, sub, "context");

            reader = new Reader(cache,logger,host,port);
        }
    }

    public Object getAndDelete(String key) {
        Object obj = cache.peek(key);
        if (obj != null) {
            del(key);
            return obj;
        }

        obj = cacheDb.peek(key);
        cacheDb.remove(key);
        return obj;
    }

    public boolean loadComplete() {
        if (reader == null)
            return true;
        return reader.isComplete();
    }

    public void setSharedObject(String pub, String sub) throws Exception {
        pub += "&context";
        freq = new ZPublisher(pub);
        sfreq = new MSubscriber(this, sub, "context");

    }

    public void setSharedObject(String pub, String sub, String host, int port) throws Exception {
        pub += "&context";
        freq = new ZPublisher(pub);
        sfreq = new MSubscriber(this, sub, "context");

        reader = new Reader(cache,logger,host,port);
    }

    public void setSharedObject(String host, int ipub, int isub, int port) throws Exception {
        String pub = "tcp://" + host + ":" + ipub + "&context";
        String sub = "tcp://" + host + ":" + isub;
        freq = new ZPublisher(pub);
        sfreq = new MSubscriber(this, sub, "context");

        reader = new Reader(cache,logger,host,port);
    }

    /**
     * Return the User object (as a map) from the database.
     *
     * @param name String. the name of the user.
     * @return ConcurrentHashMap. The map representation of the user.
     * @throws Exception on cache2k/aerorpike errors.
     */
    public ConcurrentHashMap getMap(String name) throws Exception {
        Object obj = cache.peek(name);
        if (obj != null) {
            return (ConcurrentHashMap) obj;
        }

        return (ConcurrentHashMap) cacheDb.peek(name);
    }

    /**
     * Return a Set of Strings from the cache
     *
     * @param name String . The name of the set.
     * @return Set. A set of strings.
     * @throws Exception on cache2k/aerospike errors.
     */
    public Set<String> getSet(String name) throws Exception {
        Set<String> set = (Set<String>) cache.peek(name);
        if (set == null) {
            set = (Set) cacheDb.peek(name);
        }
        return set;
    }

    /**
     * Add a map (a user map) to the the cache or aerorspike.
     *
     * @param name String. The name of the map.
     * @param map  Map. The map of the User object.
     * @throws Exception on cache2k/aerospike errors
     */
    public void addMap(String name, Map map) throws Exception {
        if (cache.peek(name) != null) {
            del(name);
        }
        cacheDb.put(name, map);
    }

    /**
     * Ass a set of strings to the cache or aerospike. Used for blacklists.
     *
     * @param name String. The name of the set of strings.
     * @param set  Set. The set of strings.
     * @throws Exception
     */
    public void addSet(String name, Set set) throws Exception {
        if (cache.peek(name) != null) {
            del(name);
        }
        cacheDb.put(name, set);
    }

    /**
     * Delete a bid key from the cache/aerospile.
     *
     * @param skey String. The key name.
     * @throws Exception on Aerospike/cache errors.
     */
    public void del(String skey) {
        if (cache.peek(skey) != null) {
            cache.remove(skey);
            Map mx = new HashMap();
            mx.put("origin",self);
            mx.put("command", "del");
            mx.put("key", skey);
            freq.add(mx);
            return;
        }
        cacheDb.remove(skey);
    }

    /**
     * Set a key value as string.
     *
     * @param skey  String. The key name.
     * @param value String. The value.
     * @throws Exception on aerorpike or cache errors.
     */
    public void set(String skey, Object value) {
        if (cache.peek(skey) != null) {
            del(skey);
        }
        cacheDb.put(skey, value);
    }

    /**
     * Set a key value as string with an expiration (No expiration set on cache2k, it is already set
     *
     * @param skey   String. The key name.
     * @param value  String. The value.
     * @param expire int. The number of seconds before expiring.
     * @throws Exception on aerorpike or cache errors.
     */
    public void set(String skey, Object value, long expire) throws Exception {

        if (cacheDb.peek(skey) != null) {
            cacheDb.remove(skey);
        }

        long ttl = System.currentTimeMillis() +
                TimeUnit.SECONDS.toMillis(expire);

        cache.invoke(skey,
                e -> e.setValue(value).setExpiry(ttl));
        Map mx = new HashMap();
        mx.put("origin",self);
        mx.put("command", "set");
        mx.put("key", skey);
        mx.put("payload", value);
        mx.put("expire", ttl);
        freq.add(mx);
    }


    /**
     * Given a key, return the string value.
     *
     * @param skey String.
     * @return String. The value of the key.
     */
    public Object get(String skey) throws Exception {
        Object obj = cache.peek(skey);
        if (obj == null) {
            obj = cacheDb.peek(skey);
            if (obj == null)
                return null;
        }
        if (obj instanceof AtomicLong) {
            Long x = ((AtomicLong) obj).get();
            return x;
        }
        return obj;
    }

    /**
     * Given a key, return the number value.
     *
     * @param skey String.
     * @return Number. The value of the key.
     */
    public Number getNumber(String skey) throws Exception {
        Object obj = cache.peek(skey);
        if (obj == null)
            obj = cacheDb.peek(skey);
        if (obj == null)
            return null;

        return (Number) obj;
    }

    /**
     * Mimic a REDIS hgetAll operation.
     *
     * @param id String. They key to get.
     * @return Map. The map stored at 'key'
     * @throws Exception on aerospike/cache2k errors.
     */
    public Map hgetAll(String id) {
        Object obj = cache.peek(id);
        if (obj == null) {
            obj = cacheDb.peek(id);
            if (obj == null)
                return null;
        }
        return (Map) obj;
    }

    /**
     * Mimic a REDIS mhset operation.
     *
     * @param id String. The key of the map.
     * @param m  Map. The map to set.
     */
    public void hmset(String id, Map m) throws Exception {
        if (cache.peek(id) != null) {
            del(id);
        }
        cacheDb.put(id, m);
    }

    /**
     * Do a mhset with expire (No op on cache2k, expiry already set globally
     *
     * @param id     String. The key name.
     * @param m      Map. The value to set.
     * @param expire long. The number of seconds before expiry.
     * @throws Exception on Cache2k or aerospike errors.
     */
    public void hmset(String id, Map m, long expire) throws Exception {

        long ttl = System.currentTimeMillis() +
                TimeUnit.SECONDS.toMillis(expire);

        cache.invoke(id,
                e -> e.setValue(m).setExpiry(ttl));

        Map mx = new HashMap();
        mx.put("origin",self);
        mx.put("command", "hmset");
        mx.put("key", id);
        mx.put("payload", m);
        mx.put("expire", ttl);
        freq.add(mx);
        return;
    }

    /**
     * Mimic a REDIS incr operation.
     *
     * @param id String. The key value to increment.
     * @return long. The incremented value. Returns 1 if id didn't exist.
     * @throws Exception on cache2k or aerospike errors.
     */
    public long incr(String id) throws Exception {
        Object x = cache.peek(id);
        if (x != null) {
            if (x instanceof AtomicLong == false) {
                throw new Exception("Can't increment " + id + " wrong type");
            }
            AtomicLong v = (AtomicLong)x;
            long value = v.addAndGet(1);

            Map mx = new HashMap();
            mx.put("origin",self);
            mx.put("command", "incr");
            mx.put("key", id);
            mx.put("payload", v);
            mx.put("expire", -1L);
            freq.add(mx);
            return value;
        }

        Long v = (Long) cacheDb.peek(id);
        if (v == null) {
            v = new Long(0);
        }
        v++;
        cacheDb.put(id, v);
        return v;
    }

    /**
     * Mimic a REDIS increment operation.
     *
     * @param id          String. The key value to increment.
     * @param ttl         int. time-to-live value in Seconds.
     * @param capTimeUnit String. Cap time unit, will be one of ("minutes", "hours", "days", "lifetime").
     * @return long. The incremented value. Returns 1 if id didn't exist.
     * @throws Exception on cache2k or aerospike errors.
     */
    public long increment(String id, long ttl, String capTimeUnit) throws Exception {
        long value = 0;
        long expire = -1;
        AtomicLong v = (AtomicLong) cache.peek(id);
        if (v == null) {
            v = new AtomicLong(1);
            final AtomicLong vv = v;

            expire = System.currentTimeMillis() +
                    TimeUnit.SECONDS.toMillis(ttl);

            long fx = expire;

            cache.invoke(id,
                    e -> e.setValue(vv).setExpiry(fx));

            value = v.get();
        } else {
            value = v.addAndGet(1);
        }

        Map mx = new HashMap();
        mx.put("origin",self);
        mx.put("command", "incr");
        mx.put("key", id);
        mx.put("payload", value);
        mx.put("expire", expire);
        freq.add(mx);

        return value;
    }

    /**
     * Expire a key (no op on Cache2k, expirt is set globally for it).
     *
     * @param id     String. The key to expire.
     * @param expire long. The number of seconds before expiration.
     * @throws Exception on cache2k or Aerorpike errors.
     */
    public void expire(String id, long expire) throws Exception {
        Object obj = cacheDb.get(id);
        if (obj != null) {
            cacheDb.remove(id);
            set(id,obj,expire);
        }
    }

    public void addList(String id, List list)   {
        cacheDb.put(id, list);
    }

    public void addListExpire(String id, List list, long expire) throws Exception {
        cacheDb.remove(id);
        set(id,list,expire);
    }

    /**
     * Return a list from the aerorpike or cache2k.
     *
     * @param id String. The key to get.
     * @return List. The list to return.
     */
    public List getList(String id) throws Exception {
        Object o = cacheDb.peek(id);
        if (o != null) {
            return (List) o;
        } else {
           o = cache.get(id);
           if (o == null)
               return null;
        }
        if (o instanceof List == false)
            throw new Exception(id + " isn't a list");
        return (List)o;
    }

    @Override
    public void handleMessage(String topic, String msg) {
        if (msg.contains("Ping"))
            return;
        msg = "{" + msg.substring(1);
        Map x = null;
        try {
            x = (Map) mapper.readValue(msg, Object.class);
        } catch (Exception error) {
            error.printStackTrace();
            return;
        }
        String origin = (String)x.get("origin");
        if (origin.equals(self))
            return;

        String key = (String) x.get("key");
        Number expire = (Number) x.get("expire");
        String cmd = (String) x.get("command");
        Object payload = x.get("payload");

        logger.debug("Cache got an update. Topic: {}. id: {}. expire: {}. cmd: {}. payload: {}", topic, key, expire, cmd, payload);

        executeOnCache(cmd, key, expire, payload);
    }

    void executeOnCache(String cmd, String key, Number n, Object payload) {
        long expiration = -1;
        if (n != null)
            expiration = n.longValue();
        long expire = expiration;
        switch (cmd) {
            case "hmset":
                cache.invoke(key,
                        e -> e.setValue(payload).setExpiry(expire));
                break;
            case "set":
                cache.invoke(key,
                        e -> e.setValue(payload).setExpiry(expire));
                break;
            case "del":
                cache.remove(key);
                break;
            case "incr":
                AtomicLong v;
                if (expire == -1) {
                    v = (AtomicLong) cache.peek(key);
                    if (v == null) {
                        logger.error("Error incrementing {}, key does not exist",key);
                        return;
                    }
                    v.addAndGet(1);
                    return;
                }
                v = new AtomicLong(1);
                cache.invoke(key,
                        e -> e.setValue(v).setExpiry(expire));
                break;
            default:
                logger.error("Unknown distributed cache command: {} for key: {}.",cmd, key);

        }
    }

    /**
     * No op, not used only for redisson compatibility.
     */
    public void shutdown() {

    }

    public static int getTimeToLiveInSecondsRoundedToNearestTimeUnitBaseOnUtcClock(int capTimeout, String capTimeUnit) {
        Instant now = Instant.now(clock);
        Instant then = now.plusSeconds(capTimeout);
        long ttl = 1;
        switch (capTimeUnit) {
            case "minutes":
                ttl = then.truncatedTo(ChronoUnit.MINUTES).getEpochSecond() - now.getEpochSecond();
                break;
            case "hours":
                ttl = then.truncatedTo(ChronoUnit.HOURS).getEpochSecond() - now.getEpochSecond();
                break;
            case "days":
                ttl = then.truncatedTo(ChronoUnit.DAYS).getEpochSecond() - now.getEpochSecond();
                break;
            default:
                ttl = then.getEpochSecond() - now.getEpochSecond();
        }
        // A check to avoid -2,-1,0 values because Aerospike has different meaning for these values.
        ttl = ttl > 0 ? ttl : 1;
        return (int) ttl;
    }

}

class Reader implements Runnable {

    private Socket s;
    private Thread me;
    private DataInputStream dis;
    byte[] buffer = new byte[4096];
    volatile Cache cache;
    Logger logger;
    String host;
    int port;
    boolean complete = false;
    public Reader(Cache cache, Logger logger, String host, int port) throws Exception {
        this.cache = cache;
        this.logger = logger;
        this.host = host;
        this.port = port;
        me = new Thread(this);
        me.start();
    }

    public boolean isComplete() {
        return complete;
    }

    public void run() {
        byte [] blen = new byte[6];
        byte [] data = new byte[4096];
        int len = 0;
        int count = 0;

        boolean connected = false;
        try {
            s = new Socket(host, port);
            dis = new DataInputStream(s.getInputStream());
            connected = true;
        } catch (Exception error) {
            logger.error("Error connecting to Zerospike reader {}/{}: {}",host,port,error.toString());
            logger.error("*** System is stopping to try to recover ***");
            if (Configuration.getInstance() != null)
                RTBServer.panicStop();
            System.exit(1);
        }

        long time = System.currentTimeMillis();
        try {
            while (true) {
                int rc = 0;
                len = 6;
                while(rc != len) {
                    int k = dis.read(blen,rc,len-rc);
                    if (k <= 0)
                        break;
                    rc += k;
                }
                if (rc != 6)
                    break;
                String str = new String(blen,0,len);
                str = str.replaceAll(" ","");
                len = Integer.parseInt(str);

                if (len > data.length)
                    data = new byte[len];
                int x = 0;
                rc = 0;
                while(rc != len) {
                    rc += dis.read(data,rc,len-rc);
                }
                str = new String(data,0,len);
                Map map = null;
                try {
                    map = (Map) RedissonClient.mapper.readValue(str, Map.class);
                } catch (Exception error) {
                    error.printStackTrace();
                    return;
                }
                String key = (String) map.get("key");
                Number expire = (Number) map.get("expire");
                String cmd = (String) map.get("command");
                Object payload = map.get("payload");
                executeOnCache(cmd,key,expire,payload);

                count++;
            }
            time = System.currentTimeMillis() - time;
            time /= 1000;
            logger.info("Initialization reader completed, with {} objects in {} seconds",count,time);
        } catch (Exception error) {
            error.printStackTrace();
            System.out.println("LEN: " + len);
            logger.error("Error reading initial data, error: {}",error.toString());
        }
        complete = true;
    }

    void executeOnCache(String cmd, String key, Number n, Object payload) {
        long expiration = -1;
        if (n != null)
            expiration = n.longValue();
        long expire = expiration;
        switch (cmd) {
            case "hmset":
                cache.invoke(key,
                        e -> e.setValue(payload).setExpiry(expire));
                break;
            case "set":
                cache.invoke(key,
                        e -> e.setValue(payload).setExpiry(expire));
                break;
            case "incr":
                Number value = (Number) payload;
                AtomicLong v = new AtomicLong(value.longValue());
                cache.invoke(key,
                        e -> e.setValue(v).setExpiry(expire));
                break;
            default:
                logger.error("Unknown distributed cache command: {} for key: {}.", cmd, key);

        }
    }

}
