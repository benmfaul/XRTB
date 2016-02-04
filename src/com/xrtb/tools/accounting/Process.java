package com.xrtb.tools.accounting;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
		Integer startMonth = 1;
		Integer stopMonth = 12;
		String content = null;
		Year year = new Year(2016);
		
		int i = 0;
		while (i < args.length) {
			switch(args[i]) {
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
			default:
				System.err.println("Huh?");
				System.exit(0);
			}
		}
		
		FileReader fr = new FileReader(source); 
		BufferedReader bufr = new BufferedReader(fr); 
		
		
		for (i=0; i< filter.size();i++) {
			while((content = bufr.readLine()) != null) {
				Record record = mapper.readValue(content, Record.class);
				if (record.name.equals("virus")) {
					record.process();
					year.process(record);
				}
			}
		}
		
		year.print(startMonth,stopMonth);
	}
	
	public Process() {
		
	}
}