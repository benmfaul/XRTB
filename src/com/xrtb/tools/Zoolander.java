package com.xrtb.tools;
import org.apache.zookeeper.Watcher.Event.EventType;

/**
 * Interface for catch Zookeeper data events
 * @author Ben M. Faul
 *
 */
public interface Zoolander {
	public void callBack(String name, EventType etype) throws Exception ;
}
