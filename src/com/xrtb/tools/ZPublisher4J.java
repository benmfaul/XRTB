package com.xrtb.tools;

import java.text.SimpleDateFormat;


import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import com.xrtb.common.Configuration;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import com.xrtb.bidder.ZPublisher;

/**
 * A class to log log4j messages to memory, for use by the web api.
 * See: https://mytechattempts.wordpress.com/2011/05/10/log4j-custom-memory-appender
 * @author Ben M. Faul
 *
 */

public class ZPublisher4J extends AppenderSkeleton {
	/** Formatter for the date time string */
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	/** The publisher to be used by this Log4j appender */
	ZPublisher publisher;
	
	/**
	 * Set the Publisher using the url string
	 * @param str String. The ZPublisher string.
	 */
	public void setPublisher(String str) {
		try {
			publisher = new ZPublisher(Configuration.substitute(str));
		} catch (Exception error) {
			error.printStackTrace();
			System.out.println("Log4j failed to open ZPublisher: " + str);
		}
	}
	/**
	 * Close the appender
	 */
	public synchronized void close() {
		if (this.closed) {
			return;
		}
		this.closed = true;
	}

	/**
	 * Indicate we want the layout
	 */
	public boolean requiresLayout() {
		return true;
	}

	/**
	 * Make sure we can append
	 * @return boolean. Returns true if we are accepting logs.
	 */
	protected boolean checkEntryConditions() {
		if (this.closed) {
			return false;
		}
		return true;
	}

	/**
	 * Append the log to ZPublisher
	 * @param event LoggingEvent. The log event to format and send to the log receiver.
	 */
	protected void append(LoggingEvent event) {
		if (publisher == null)
			return;
		String name = event.getLocationInformation().getClassName();
		name = name.substring(name.lastIndexOf(".")+1);
		String level = event.getLevel().toString();
		String message = event.getRenderedMessage();
		String line = event.getLocationInformation().getLineNumber();
		Map s = new HashMap();
		s.put("sev", level);
		s.put("source", name);
		s.put("field", line);
		s.put("message", message);
		publisher.add(s);
	}
}