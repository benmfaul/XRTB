package com.xrtb.tools.logmaster;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * A simple class that logs channel information to a file, based on the channel
 * name. You will be called at execute() every logging interval. The name is the
 * channel name, the values is a list of JSON strings that are the log entries
 * for this channel for the log period.
 * 
 * @author Ben M. Faul
 *
 */

public class FileLogger extends AbstractSparkLogger {
	/** The timestamp part of the name */
	String tailstamp = "";
	/** Logger time, how many minutes before you clip the log */
	protected int time;
	/** count down time */
	protected long countdown;
	/** Logging formatter yyyy-mm-dd-hh:ss part. */
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm");

	/**
	 * The constructor for the file logger
	 */
	public FileLogger(int interval) {
		super(interval);
	}

	/**
	 * Constructor for the file logger using log names
	 * 
	 * @param interval
	 *            int. The interval for dumping the logging
	 * @param countdown
	 *            int. The interval for rolling the log
	 */
	public FileLogger(int interval, int countdown) {
		super(interval);
		this.time = countdown;
		this.time *= 60000;
		setTime();
	}

	void setTime() {
		countdown = System.currentTimeMillis() + time;
		tailstamp = "-" + sdf.format(new Date());
		System.out.println("************ SET NEW TIMESTAMP ************");
	}

	/**
	 * Time to log something.
	 * 
	 * @param name.
	 *            String. The channel name.
	 * @param values
	 *            List. A list of JSON strings.
	 */
	public void execute(String name, List<String> values) {
		StringBuilder sb = new StringBuilder();

		if (countdown != 0 && System.currentTimeMillis() > countdown) {
			setTime();
		}

		for (String contents : values) {
			sb.append(contents);
			sb.append("\n");
		}
		if (sb.length() > 0)
			try {
				String thisFile = Spark.logDir + "/" + name + tailstamp;
				System.out.println("APPENDING: " + thisFile);
				
				AppendToFile.item(thisFile, sb);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		// System.out.println("\t" + sb.toString());
	}

}
