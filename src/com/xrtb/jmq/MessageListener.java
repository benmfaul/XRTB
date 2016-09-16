package com.xrtb.jmq;
public interface MessageListener<M>  {

    /**
     * Invokes on every message in topic
     *
     * @param msg topic message
     */
    public void onMessage(String channel, M msg);

}