package com.xrtb.tools;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MakeVastCreative {

	public static void main(String [] args) throws Exception {
		if (args.length == 0) {
			args = new String[1];
			args[0] = "vast/onion270.xml";
		}
		String content = new String(Files.readAllBytes(Paths.get(args[0])),StandardCharsets.UTF_8);
		String lines[] = content.split("\n");
		
		List<String> res = new ArrayList();
		for (int i=0; i < lines.length; i++) {
			if (lines[i].length() > 0) {
				lines[i] = lines[i].replaceAll("\"", "\\\\\"");
				res.add(lines[i]);
			}
		}
		String output = "\"adm\":[   \"" + res.get(0) + "\",\n";
		for (int i = 1; i < res.size(); i++) {
			String s = res.get(i);
			output +=  "\"" + s + "\"";
			if (i + 1 < res.size()) {
				output += ",\n";
			} else {
				output += "\n";
			}
		}
		output += "]";
		System.out.println(output);
	}
}
