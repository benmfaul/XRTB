package com.xrtb.tools.logmaster;


import java.io.*;

/**
 * Appends stuff to a file
 * @author Ben M. Faul
 */
public class AppendToFile {
	
	public static void item(String fileName, StringBuilder sb)
			throws Exception {

		BufferedWriter bw = null;
		bw = new BufferedWriter(new FileWriter(fileName, true));
		bw.append(sb);
		bw.flush();
		bw.close();
	}

} 
