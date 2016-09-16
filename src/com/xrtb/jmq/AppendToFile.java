package com.xrtb.jmq;


//JavaFileAppendFileWriterExample.java
//Created by <a href="http://alvinalexander.com" title="http://alvinalexander.com">http://alvinalexander.com</a>

import java.io.*;

public class AppendToFile {

	public static void item(String fileName, StringBuilder sb)
			throws Exception {

		BufferedWriter bw = null;
		bw = new BufferedWriter(new FileWriter(fileName, true));
		bw.write(sb.toString());
		bw.flush();
		bw.close();
	}

} 
