package com.xrtb.services;

import java.io.Serializable;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * A class that holds a key to be deleted, postponed to a later time.
 * @author Ben M. Faul.
 */
public class PostponedWorkItem implements Delayed, Serializable {
    private  long startTime;
    private  String workItem;

    public PostponedWorkItem( final String workItem, final long delay ) {
        super();
        this.workItem = workItem;
        this.startTime = delay;
    }

    public PostponedWorkItem() {

    }

    public long getStartTime() {
        return startTime;
    }

    public String getEWorkItem() {
        return workItem;
    }

    public String getKey() {
        return workItem;
    }

    @Override
    public long getDelay( TimeUnit unit ) {
        long diff = startTime - System.currentTimeMillis();
        // System.out.println(workItem + " DIFF: " + diff);
        return diff;

    }

    @Override
    public int compareTo( Delayed delayed ) {
        if (this.startTime < ((PostponedWorkItem)delayed).startTime)
            return -1;

        if (this.startTime > ((PostponedWorkItem)delayed).startTime)
            return 1;

        return 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;

        int result = 1;
        result = prime * result + ( ( workItem == null ) ? 0 : workItem.hashCode() );

        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if( this == obj ) {
            return true;
        }

        if( obj == null ) {
            return false;
        }

        if( !( obj instanceof PostponedWorkItem ) ) {
            return false;
        }

        final PostponedWorkItem other = ( PostponedWorkItem )obj;
        if( workItem == null ) {
            if( other.workItem != null ) {
                return false;
            }
        } else if( !workItem.equals( other.workItem ) ) {
            return false;
        }

        return true;
    }
}
