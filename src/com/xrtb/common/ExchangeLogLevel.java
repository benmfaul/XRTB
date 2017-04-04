package com.xrtb.common;

import java.util.HashMap;
import java.util.Map;

import com.xrtb.tools.XORShiftRandom;

/**
 * Class that determines if a request log should be released. By default, all requests will be released. You can
 * set the percentage to globally log. You can however override the global request percentage by specfying the
 * log level in the creation of the endpoint.
 * @author Ben M. Faul
 *
 */
public enum ExchangeLogLevel {
	// The instance
	INSTANCE;

	// The exchange level logging
	Map<String, Integer> levels = new HashMap<String,Integer>();
	
	// Global request log strategy
	int requestLogPercentage = 100;
	
	// Random numbers 0 - 100
	final XORShiftRandom xorrandom = new XORShiftRandom();
	
	/**
	 * Returns the instance.
	 * @return Exchange logger.
	 */
	public static ExchangeLogLevel getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Get the log level for an exchange.
	 * @param exchange String. Get the exchange level.
	 * @return Integer. The log level. 50 means 50%
	 */
	public Integer getLevel(String exchange) {
		Integer level = levels.get(exchange);
		if (level == null)
			return requestLogPercentage;
		return level;
	}
	
	/**
	 * Set the global logging level for requests.
	 * @param level
	 */
	public void setStdLevel(int level) {
		requestLogPercentage = level;
	}

	/**
	 * Set the log level for an exchange's requests.
	 * @param exchange String. The exchange in question.
	 * @param level int. The level to log at 100 means all, 50 means 50%, for example.
	 */
	public void setExchangeLogLevel(String exchange, int level) {
		levels.put(exchange, level);
	}
	
	/**
	 * Should this request be logged.
	 * @param exchange String. The exchaange in question.
	 * @return boolean. True means log it, false means don't log it.
	 */
	public boolean shouldLog(String exchange) {
		Integer level = getLevel(exchange);
		if (level == null)
			level = requestLogPercentage;
		
		if (level == 100)
			return true;
		
		int value = xorrandom.random(100);
		if (! (requestLogPercentage >= value)) {
			return false;
		}
		return true;

	}
}
