package com.xrtb.tools.acct;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.pojo.BidResponse;
import com.xrtb.pojo.WinObject;

/**
 * A simple accounting program for processing Accounting records from Spark.
 * 
 * @author Ben M. Faul
 *
 */
public class Process {

	/** Converts Accounting record from Spark to a hashmap */
	public static ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	static boolean nz = false;

	/**
	 * Main interface for the accounting process.
	 * 
	 * @param args
	 *            String[]. The accounts processing. Arguments: -source
	 *            <filename> Set the input filename. Default is logs/accouunting
	 *            -year <year> Set the year. Default is this year -startMonth
	 *            <month> Set the month to start the report. Default is current
	 *            momth. Jan = 1 -endMonth <month> Set the month to end the
	 *            report. Default is current month. Jan = 1 -hourly Do hourly
	 *            instead of daily reports. Defaults to not hourly (by day) -day
	 *            <day of month> Set the days to do the report on (within
	 *            months). Default is today. Can specify multiple days like -day
	 *            1 -day 2 -day 3 Writes report to stdout. Defaults to
	 *            -startMonth <now> -stopMonth <now> -year <now> -source
	 *            logs/accounting
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {
		String source = "logs/accounting";
		List<Integer> days = null;
		Integer startMonth = 1;
		Integer stopMonth = 12;
		String content = null;
		boolean hourly = false;
		String csvName = null;

		StringBuilder csv = null;

		Year year = null;

		SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));

		// Local time zone
		SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");

		// Time in GMT
		Date now = dateFormatLocal.parse(dateFormatGmt.format(new Date()));
		int yr = 1900 + now.getYear();
		int month = 1 + now.getMonth();
		int day = now.getDate();

		if (args.length == 0) {

			args = new String[9];
			args[0] = "-year";
			args[1] = "" + yr;
			args[2] = "-startMonth";
			args[3] = "" + month;
			args[4] = "-stopMonth";
			args[5] = "" + month;
			args[6] = "-day";
			args[7] = "" + day;
			args[8] = "-hourly";
		}

		int i = 0;
		while (i < args.length) {
			switch (args[i]) {
			case "-year":
				year = new Year(Integer.parseInt(args[i + 1]));
				i += 2;
				break;
			case "-source":
				source = args[i + 1];
				if (year == null)
					year = new Year(yr);
				i += 2;
				break;
			case "-startMonth":
				startMonth = Integer.parseInt(args[i + 1]);
				i += 2;
				break;
			case "-stopMonth":
				stopMonth = Integer.parseInt(args[i + 1]);
				i += 2;
				break;
			case "-day":
			case "-date":
				if (days == null)
					days = new ArrayList();
				days.add(Integer.parseInt(args[i + 1]));
				i += 2;
				break;
			case "-hourly":
				hourly = true;
				i++;
				break;
			case "-csv":
				csvName = args[i + 1];
				i += 2;
				break;
			case "-exchange":
				Hour.exchange = args[i + 1];
				i += 2;
				break;
			case "-nz":
				nz = true;
				i++;
				break;
			case "-yesterday":
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DATE, -1);
				Date date = cal.getTime();
				startMonth = date.getMonth() + 1;
				stopMonth = startMonth;
				days = new ArrayList();
				days.add(date.getDate());

				i++;

				break;
			case "-h":
			case "-help":
				System.out.println("Sample accounting program for use with Spark. Usage:\n");
				System.out.println("-source <filename>    Set the input filename. Default is logs/accouunting");
				System.out.println("-year <year>          Set the year. Default is this year");
				System.out.println("-startMonth  <month>  Set start month of the report. Default is current month.");
				System.out.println("                      Jan  = 1");
				System.out.println("-endMonth   <month>   Set the month to end the report. Default is current month.");
				System.out.println("                      Jan = 1");
				System.out.println("-day <day of month>   Set the days to do the report on (within months");
				System.out.println("                      Default is today");
				System.out.println("                      Can specify multiple days like -day 1 -day 2 -day 3");
				System.out.println("-hourly               Specifies to do hourly reports, else Daily");
				System.out.println("                      Defaults to hourly");
				System.out.println("-csv <filename>       Output the data to a CSV file (as well as stdout. ");
				System.out.println("                      Default is no CSV");
				System.out.println("-yesterday            Set startMonth, endMonth and day to yeserday's particulars");
				System.out.println("-exchange <name>      Process just for this exchange.");
				System.out.println("\nWrites report to stdout");
				System.out.println("Default: -startMonth " + month + " -stopMonth " + month + " -year " + yr
						+ " -hourly -source logs/accounting");
				System.exit(0);
			default:
				System.err.println("Huh? " + args[i]);
				System.exit(0);
			}
		}

		FileReader fr = null;
		BufferedReader bufr = null;

		File folder = new File(source);
		File[] listOfFiles = folder.listFiles();

		int threads = 8;
		List<FileProcessor> procs = new ArrayList();
		List<String> names = new ArrayList();
		
		System.out.println("File coount = " + names.size());

		
		for (File f : listOfFiles) {
			if (f.isFile()) {
				if (f.getName().startsWith("bids") || f.getName().startsWith("wins")
						|| f.getName().startsWith("request")) {

					//FileProcessor p = new FileProcessor(f.getPath());
					//procs.add(p);
					names.add(f.getPath());
				}
				/*
				 * fr = new FileReader(f); bufr = new BufferedReader(fr); while
				 * ((content = bufr.readLine()) != null) { Object x = null; if
				 * (f.getName().startsWith("bids")) x =
				 * mapper.readValue(content, BidResponse.class); else if
				 * (f.getName().startsWith("wins")) x =
				 * mapper.readValue(content, WinObject.class); else if
				 * (f.getName().startsWith("request")) x =
				 * mapper.readValue(content, Map.class);
				 * 
				 * Record record = Record.getInstance(x); } }
				 */
			}
			
			
		/*	while (procs.size() > 0) {
				for (FileProcessor p : procs) {
					if (p.done) {
						System.out.println("Completed: " + p.fileName);
						procs.remove(p);
						break;
					}	
				}
			} */
		}
		
		long time = System.currentTimeMillis();
		while(names.size()>0 || procs.size()>0) {
			if (names.size() != 0 && procs.size()<threads) {
				FileProcessor p = new FileProcessor(names.get(0));
				procs.add(p);
				System.out.println("Starting: " + names.get(0));
				names.remove(0);
			}
			for (FileProcessor p : procs) {
				if (p.done) {
					System.out.println("Completed: " + p.fileName);
					procs.remove(p);
					break;
				}	
			}
		}
		time  = System.currentTimeMillis() - time;
	//	System.out.println(time);

		Iterator it = Record.records.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Record> pair = (Map.Entry) it.next();
			;
			year.process(pair.getValue());
		}

		if (csvName != null) {
			csv = new StringBuilder();
			if (!hourly)
				csv.append("year,month,day,requests,bids,wins,bidprice,winprice,pixels,clicks\n");
			else
				csv.append("year,month,day,hour,requests,bids,wins,bidprice,winprice,pixels,clicks\n");

		}
		year.print(startMonth, stopMonth, days, hourly, csv);

		if (csvName != null) {
			content = csv.toString();
			Files.write(Paths.get(csvName), content.getBytes());
			System.out.println("\n----------------\nCSV: " + csvName + "\n");
			System.out.println(content);
		}
	}

	public Process() {

	}
}

class FileProcessor implements Runnable {
	static ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	String fileName;
	Thread me;
	public String status;
	public boolean done = false;

	public FileProcessor(String fileName)  {
		this.fileName = fileName;
		me = new Thread(this);
		me.start();
	}

	public void run() {
		long time = System.currentTimeMillis();
		String content = null;
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			while ((content = br.readLine()) != null) {
				Object x = null;
				try {
				if (fileName.contains("bids"))
					x = mapper.readValue(content, BidResponse.class);
				else if (fileName.contains("wins"))
					x = mapper.readValue(content, WinObject.class);
				else if (fileName.contains("request"))
					x = mapper.readValue(content, Map.class);

				Record record = Record.getInstance(x);
				} catch (Exception xxx) {
					System.out.println(xxx);
					System.out.println(content);
					
				}
			}
			status = fileName + " COMPLETE. Time = ";
		} catch (Exception error) {
			System.out.println(error);
			status = fileName + " ERROR: " + error.toString();

		} finally {
			done = true;
		}

	}

	public String getStatus() {
		return status;
	}

}