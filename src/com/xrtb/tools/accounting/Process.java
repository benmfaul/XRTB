package com.xrtb.tools.accounting;

import java.io.BufferedReader;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A simple accounting program for processing Accounting records from Spark.
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
	 * @param args String[]. The accounts processing.
	 * Arguments:
	 * -source <filename>    Set the input filename. Default is logs/accouunting
	 * -year <year>          Set the year. Default is this year
	 * -startMonth  <month>  Set the month to start the report. Default is current momth. Jan  = 1
	 * -endMonth   <month>   Set the month to end the report. Default is current month. Jan = 1
	 * -hourly               Do hourly instead of daily reports. Defaults to not hourly (by day)
	 * -day <day of month>   Set the days to do the report on (within months). Default is today. Can specify multiple days like -day 1 -day 2 -day 3
	 * Writes report to stdout.
	 * Defaults to -startMonth <now> -stopMonth <now> -year <now> -source logs/accounting
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

		//Local time zone   
		SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");

		//Time in GMT
		Date now = dateFormatLocal.parse( dateFormatGmt.format(new Date()) );
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
			args[7] = ""+day;
			args[8] = "-hourly";
		}
		
		int i = 0;
		while (i < args.length) {
			switch(args[i]) {
			case "-year":
				year = new Year(Integer.parseInt(args[i+1]));
				i+=2;
				break;
			case "-source":
				source = args[i+1];
				if (year == null)
					year = new Year(yr);
				i+= 2;
				break;
			case "-startMonth":
				startMonth = Integer.parseInt(args[i+1]);
				i+= 2;
				break;
			case "-stopMonth":
				stopMonth = Integer.parseInt(args[i+1]);
				i+= 2;
				break;	
			case "-day":
			case "-date":
				if (days == null)
					days = new ArrayList();
				days.add(Integer.parseInt(args[i+1]));
				i+= 2;
				break;
			case "-hourly":
				hourly = true;
				i++;
				break;
			case "-csv":
				csvName = args[i+1];
				i+=2;
				break;
			case "-exchange":
				Hour.exchange = args[i+1];
				i+=2;
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
				 System.out.println("Default: -startMonth " + month + " -stopMonth " + month + " -year " + yr + " -hourly -source logs/accounting");
				 System.exit(0);
			default:
				System.err.println("Huh? " + args[i]);
				System.exit(0);
			}
		}
		
		FileReader fr = new FileReader(source); 
		BufferedReader bufr = new BufferedReader(fr); 
		
		if (year == null)
			year = new Year(yr);
		
		
		while((content = bufr.readLine()) != null) {
			Record record = null;
			try {
				record = mapper.readValue(content, Record.class);
				record.process();
				year.process(record);
			} catch (Exception error) {
				error.printStackTrace();
				System.err.println("Bad Record: " + content);
			} 
		}
		
		if (csvName != null) {
			csv = new StringBuilder();
			if (!hourly)
				csv.append("year,month,day,bids,wins,bidprice,winprice,pixels,clicks\n");
			else
				csv.append("year,month,day,hour,bids,wins,bidprice,winprice,pixels,clicks\n");
				
		}
		year.print(startMonth,stopMonth,days,hourly,csv);
		
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