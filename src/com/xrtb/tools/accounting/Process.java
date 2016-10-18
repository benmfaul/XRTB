package com.xrtb.tools.accounting;

import java.io.BufferedReader;

import java.io.FileReader;

import java.util.ArrayList;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Process {
	
	public static ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public static void main(String args[]) throws Exception {
		String source = "logs/accounting";
		List<Integer> days = null;
		Integer startMonth = 1;
		Integer stopMonth = 12;
		String content = null;
		boolean hourly = false;
		String csvName = null;
		Year year = new Year(2016);
		StringBuilder csv = null;
		
		if (args.length == 0) {
			args = new String[9];
			args[0] = "-year";
			args[1] = "2016";
			args[2] = "-startMonth";
			args[3] = "10";
			args[4] = "-stopMonth";
			args[5] = "10";
			args[6] = "-day";
			args[7] = "18";
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
			default:
				System.err.println("Huh?");
				System.exit(0);
			}
		}
		
		FileReader fr = new FileReader(source); 
		BufferedReader bufr = new BufferedReader(fr); 
		
		while((content = bufr.readLine()) != null) {
			Record record = null;
			try {
				record = mapper.readValue(content, Record.class);
				record.process();
				year.process(record);
			} catch (Exception error) {
				System.err.println("Bad Record: " + content);
			} 
		}
		
		if (csvName != null) {
			csv = new StringBuilder();
			csv.append("month,day,bids,wins,bidprice,winprice,pixels,clicks\n");
		}
		year.print(startMonth,stopMonth,days,hourly,csv);
		
		if (csvName != null)
			System.out.println("\n\n" + csv.toString());
	}
	
	public Process() {
		
	}
}