package com.xrtb.tools.logmaster;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Appends stuff to a file
 * 
 * @author Ben M. Faul
 */
public class AppendToFile {

	public static Map<String, BufferedWriter> files = new ConcurrentHashMap();

	public static void item(String fileName, StringBuilder sb) throws Exception {

		BufferedWriter bw;
		synchronized (files) {
			bw = files.get(fileName);
			if (bw == null) {
				FileWriter x = new FileWriter(fileName, true);
				bw = new BufferedWriter(x);
				files.put(fileName, bw);
			}
		}
		synchronized (bw) {
			try {
				bw.append(sb);
				bw.flush();
			} catch (Exception error) {
				error.printStackTrace();
				bw.close();
				files.remove(fileName);
			}
		}
	}

	public static void close(String fileName) throws Exception {
		BufferedWriter bw = files.get(fileName);
		if (bw != null)
			bw.close();
		files.remove(fileName);
	}

	public static void close() throws Exception {
		synchronized (files) {
			files.forEach((k, v) -> {
				try {
					System.out.println("LOGGER Closing file: " + k);
					v.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			files.clear();
		}
	}
}
