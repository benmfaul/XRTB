package com.xrtb.tools.logmaster;

import java.util.List;

/**
 * A simple class that logs channel information to a file, based on the channel name. You will be called
 * at execute() every logging interval. The name is the channel name, the values is a list of JSON strings that are the
 * log entries for this channel for the log period.
 * 
 * @author Ben M. Faul
 *
 */

public class FileLogger extends AbstractSparkLogger {
	/**
	 * The constructor for the file logger
	 */
	public FileLogger(int interval) {
		super(interval);
	}
	
	/**
	 * Time to log something.
	 * @param name. String. The channel name.
	 * @param values List. A list of JSON strings.
	 */
	public void execute(String name, List<String> values) {
		StringBuilder sb = new StringBuilder();

		for (String contents : values) {
			sb.append(contents);
			sb.append("\n");
		}
		if (sb.length() > 0)
			try {
				AppendToFile.item(Spark.logDir + "/" + name, sb);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		//System.out.println("\t" + sb.toString());
	}

}
