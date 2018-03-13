package com.xrtb.services;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The worker that deletes stuff out of the database as they age from the DelayQueue.
 */
class KeyDeletionScheduler {
    /** The tree interface to the data on disk */
    private volatile HTreeMap object;
    /** The delayed queue of keys that time out */
    private final BlockingQueue< PostponedWorkItem > delayed =
            new DelayQueue< PostponedWorkItem >();
    /** The trace flag */
    private boolean trace = false;
    /** The error logger object */
    private Logger logger;
    /** Counter for number of keys deleted */
    private AtomicLong count = new AtomicLong(0);

    /** A scratch dataabase we use to store off-heap items that will be deleted */
    private volatile DB scratchdb;
    /** The map of work items. stored off-heap */
    private HTreeMap<String, PostponedWorkItem> map;

    /**
     * Every 1/2 second this worker gathers all the expired keys up, then deletes them from the database file. Note,
     * it does not need to let the subscribers know, as their own caches will also delete the data.
     * @param object HTreeMap. The interface to the map database on disk.
     * @param logger Logger. The error logger.
     */
    public KeyDeletionScheduler(HTreeMap object, Logger logger) {
        this.object = object;
        this.logger = logger;

        scratchdb = DBMaker.tempFileDB()
                .fileMmapEnable()            // Always enable mmap
                .fileMmapEnableIfSupported() // Only enable mmap on supported platforms
                .fileMmapPreclearDisable()   // Make mmap file faster
                // Unmap (release resources) file when its closed.
                // That can cause JVM crash if file is accessed after it was unmapped
                // (there is possible race condition).
                .cleanerHackEnable()
                .make();

        map = (HTreeMap<String, PostponedWorkItem>) scratchdb.hashMap("scratch").createOrOpen();
        map.clear();

        ScheduledExecutorService execService = Executors.newScheduledThreadPool(5);
        execService.scheduleAtFixedRate(() -> {
            try {
                process();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }, 500, 500, TimeUnit.MILLISECONDS);
    }

    /**
     * Sets the trace flag.
     * @param t boolean. Set to true to watch activities.
     */
    public void setTrace(boolean t) {
        trace = t;
    }

    /**
     * Delete the data on disk indexed by key.
     * @param key String. The key of the data to delete.
     */
    public void delKey(final String key) {
        PostponedWorkItem x = map.get(key);
        if (x == null)
            return;
        delayed.remove(x);
    }

    /**
     * Return the number of deletions
     * @return long. The number of keys deleted.
     */
    public long getDeletions() {
        return count.getAndSet(0);
    }

    /**
     * Add a key, to delayqueue so we can decide when to delete it.
     * @param workItem String. The key to delete in the future.
     * @param time long. The future time when this key is to be deleted.
     */
    public void addKey( final String workItem, final long time ) {
        if (trace) {
            logger.info("ADD ITEM: {}, timeout: {}" + workItem, time);
        }
        final PostponedWorkItem postponed = new PostponedWorkItem( workItem, time );
        if( !delayed.contains( postponed )) {
            map.put(workItem,postponed);
            delayed.offer( postponed );
        }
    }

    /**
     * Return the absolute time (epoch) when the key will expire.
     * @param key String. The key
     * @return long. The epoch of wwhen this key actually expires.
     */
    public long getTTL(final String key) {
       final PostponedWorkItem wk =  map.get(key);
       return wk.getStartTime();
    }
    /**
     * Drain the expired keys into a bucker, then delete them. TBD: This could be improved with a thread pool.
     */
    public void process() {
        final Collection< PostponedWorkItem > expired = new ArrayList< PostponedWorkItem >();
        delayed.drainTo( expired );

        // System.out.println("EXPIRED: " + expired);

        for( final PostponedWorkItem postponed: expired ) {
            count.incrementAndGet();
            String key = postponed.getKey();
            object.remove(key);
            if (trace) {
                logger.info("Key expired: {}",key);
            }
        }
    }
}

