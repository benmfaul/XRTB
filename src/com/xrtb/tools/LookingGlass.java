package com.xrtb.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.xrtb.bidder.Controller;
import com.xrtb.common.Configuration;

public class LookingGlass {

	public static Map<String, Object> symbols = new HashMap();
	
	Map myMap = new HashMap();
	
	public LookingGlass() {
		
	}
	
	public LookingGlass(String name, String file) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(file));

		String[] parts = null;	
		String message = "Initialize Simple Map: " + file + " as " + name;
		for (String line; (line = br.readLine()) != null;) {
			parts = eatquotedStrings(line);
			for (int i=0;i<parts.length;i++) {
				parts[i] = parts[i].replaceAll("\"","");
			}
			myMap.put(parts[0], parts);
		}
		
		symbols.put(name, this);
		System.out.format("[%s] - %d - %s - %s - %s\n",Controller.sdf.format(new Date()), 1, Configuration.instanceName, this.getClass().getName(),message);
	}
	
	public Object query(Object key) {
		return myMap.get(key);
	}
	
	/**
	 * Read comma separated items, except, ignore all comments in double quoted strings.
	 * @param line String. The line to parse.
	 * @return String[]. The tokens parsed from the line.
	 */
	public static String[] eatquotedStrings(String line) {

		String otherThanQuote = " [^\"] ";
		String quotedString = String.format(" \" %s* \" ", otherThanQuote);
		String regex = String.format(
				"(?x) " + // enable comments, ignore white spaces
						",                         " + // match a comma
						"(?=                       " + // start positive look
														// ahead
						"  (?:                     " + // start non-capturing
														// group 1
						"    %s*                   " + // match 'otherThanQuote'
														// zero or more times
						"    %s                    " + // match 'quotedString'
						"  )*                      " + // end group 1 and repeat
														// it zero or more times
						"  %s*                     " + // match 'otherThanQuote'
						"  $                       " + // match the end of the
														// string
						")                         ", // stop positive look
														// ahead
				otherThanQuote, quotedString, otherThanQuote);

		String[] tokens = line.split(regex, -1);
		//for (String t : tokens) {
		//	System.out.println("> " + t);
		// }
		return tokens;
	}
}
