package com.xrtb.tools.logmaster;


import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Appends stuff to a file
 * @author Ben M. Faul
 */
public class AppendToFile {
	
	public static Map<String, BufferedWriter> files = new HashMap();
	public static void item(String fileName, StringBuilder sb)
			throws Exception {

		
		BufferedWriter bw = files.get(fileName);
		if (bw == null) {
			FileWriter x = new FileWriter(fileName, true);
			bw = new BufferedWriter(x);
			files.put(fileName, bw);
		}
		synchronized(bw) {
			bw.append(sb);
			bw.flush();
		}
	}

	
	public static void close(String fileName) throws Exception {
		BufferedWriter bw = files.get(fileName);
		if (bw != null)
			bw.close();
		files.remove(fileName);
	}
} 
