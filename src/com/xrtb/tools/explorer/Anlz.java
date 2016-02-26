package com.xrtb.tools.explorer;

import io.netty.util.internal.ConcurrentSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Anlz {

	String input;
	long lines;
	public static ObjectMapper mapper = new ObjectMapper();
	List rdd = new ArrayList();
	
	
	public static void main(String [] args) throws Exception {
		Anlz a = new Anlz();
		a.setInput("logs/request");
		a.process();
	}
	public String getInput() {
		return input;
	}
	
	public void setInput(String input) {
		this.input = input;
		File f = new File(input);
		if (!f.exists())
			System.err.println("File does not exist");
	}
	
	public List rdd() {
		return rdd;
	}
	
	public Anlz() {
		System.out.println("Anlz 1.0");
	}
	
	public long count() {
		return rdd.size();
	}
	
	public void process() throws Exception{
		Map map = null;
		long time = System.currentTimeMillis();
		System.out.println("A");
	
		Stream<String> stream = Files.lines(Paths.get(input));
		Iterator<String> iter = stream.iterator();
		while(iter.hasNext()) {
			lines++;
			//rdd.add(map);
			System.out.println(lines);
			iter.next();
		}
		
		time = System.currentTimeMillis() - time;
		System.out.println("Read " + lines + " from " + input + " in " + time + " milliseconds");
		
	}
	
}
