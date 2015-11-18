package com.xrtb.bidder;

import java.util.concurrent.CountDownLatch;

import java.util.concurrent.TimeUnit;

/**
 * A class that can 
 * @author ben
 *
 */

public class AbortableCountDownLatch extends CountDownLatch {
    protected boolean aborted = false;
    int watchers;

    public AbortableCountDownLatch(int count, int watchers) {
        super(count);
        this.watchers = watchers;
    }

    @Override
    public void countDown() {
    	super.countDown();
    	watchers--;
    	if (watchers > 0)
    		abort();
    }

   /**
     * Unblocks all threads waiting on this latch and cause them to receive an
     * AbortedException.  If the latch has already counted all the way down,
     * this method does nothing.
     */
    public void abort() {
        if( getCount()==0 )
            return;

        this.aborted = true;
        while(getCount()>0)
            countDown();
    }


    @Override
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        final boolean rtrn = super.await(timeout,unit);
        if (aborted)
            throw new AbortedException();
        return rtrn;
    }

    @Override
    public void await() throws InterruptedException {
        super.await();
        if (aborted)
            throw new AbortedException();
    }
    
    public void countNull() {
    	watchers--;
    	if (watchers == 0)
    		abort();
    }


    public static class AbortedException extends InterruptedException {
        public AbortedException() {
        }

        public AbortedException(String detailMessage) {
            super(detailMessage);
        }
    }
}