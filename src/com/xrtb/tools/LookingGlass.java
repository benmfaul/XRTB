package com.xrtb.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.xrtb.bidder.Controller;
import com.xrtb.common.Configuration;

public class LookingGlass {

	// The symbol table used throughout the bidder
	public static volatile Map<String, Object> symbols = new ConcurrentHashMap<String, Object>();
	
	// My map
	Map myMap = new ConcurrentHashMap();
	
	/**
	 * Default constructor
	 */
	public LookingGlass() {
		
	}
	
	/**
	 * A Class that implements a map from a 2 element comma separated list.
	 * @param name String. The symbol name this object is known by in the bidder.
	 * @param file String. The filename of the csv data file.
	 * @throws Exception on File I/O errors.
	 */
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
	
	/**
	 * Return the value stored at key.
	 * @param key Object. The key to use in the lookup.
	 * @return Object. Returns the value at key, or null if not in the list.
	 */
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

        //for (String t : tokens) {
		//	System.out.println("> " + t);
		// }
		return line.split(regex, -1);
	}
}
