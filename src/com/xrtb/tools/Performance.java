package com.xrtb.tools;

import java.io.File;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Class provides basic runtime specifics used in the heartbeat message of the RTB4FREE system.
 * @author Ben M. Faul
 *
 */
public class Performance {
	static long mb = 1024*1024;

	static DecimalFormat formatter = new DecimalFormat("###.###",
			DecimalFormatSymbols.getInstance(Locale.ENGLISH));
	static {
		formatter.setRoundingMode(RoundingMode.DOWN);
	}

	public static String getCpuPerfAsString() {
		OperatingSystemMXBean mx = java.lang.management.ManagementFactory
				.getOperatingSystemMXBean();
		int cores = Runtime.getRuntime().availableProcessors();
		double d = mx.getSystemLoadAverage() * 100 / cores;
		String s = formatter.format(d);
		return s;
	}
	
	public static int getCores() {
		 return Runtime.getRuntime().availableProcessors();
	}
	
	public static int getThreadCount() {
		 ThreadMXBean threadMXBean = java.lang.management.ManagementFactory.getThreadMXBean();
		return threadMXBean.getThreadCount();
	
	}
	
	public static String getPercFreeDisk() {
		
		File file = new File("/");
		
		long totalSpace = file.getTotalSpace(); //total disk space in bytes.
    	long usableSpace = file.getUsableSpace(); ///unallocated / free disk space in bytes.
    	long freeSpace = file.getFreeSpace(); //unallocated / free disk space in bytes.
    	
    	double percent = (double)freeSpace/(double)totalSpace * 100;
    	String s = formatter.format(percent);
    	return s;
	}
	
	public static String getMemoryUsed() {
		Runtime runtime = Runtime.getRuntime();
		long memory = runtime.totalMemory() - runtime.freeMemory();
		double perc = memory / (double)runtime.maxMemory() * 100;
		memory /= mb;
		String s = formatter.format(perc);
		return Long.toString(memory) + "M (" + s + "%)";
		
	}
}
