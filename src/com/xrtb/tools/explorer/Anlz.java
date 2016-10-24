package com.xrtb.tools.explorer;

import java.io.File;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;


public class Anlz extends ArrayList {

	String input;
	long lines;
	List<ANode> filters = new ArrayList();
	List<Counter> counters = new ArrayList();
	boolean addCr;
	public static ObjectMapper mapper = new ObjectMapper();
	
	
	public static void main(String [] args) throws Exception {
		Anlz a = new Anlz();
		a.addCr = true;
		a.standard();
	}
	
	public void html() {
		
	}
	
	
	public void standard() throws Exception {
		//setInput("logs/request");
		setInput("/home/ben/workspace/request.json");
		setFilter("bcat","NOT_MEMBER","XXX");
		
		List<String> h = new ArrayList();
		setCounter("imp.0.banner.w","imp.0.banner.h").setSep("x");
		setCounterUnique("imp.0.banner.battr");
		setCounterUnique("imp.0.banner.mimes");
		setCounter("site.domain");
		setCounterUnique("bcat");
		setCounterUnique("site.cat");
		setCounter("app.domain");
		setCounter("device.os");
		setCounter("device.make");
		setCounter("device.geo.country");
		process();
		report();
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
	
	public Anlz() {
		System.out.println("Anlz 1.0");
	}
	
	public void process() throws Exception{
		Map map = null;
		long time = System.currentTimeMillis();
	
		Stream<String> stream = Files.lines(Paths.get(input));
		Iterator<String> iter = stream.iterator();
		while(iter.hasNext()) {
			lines++;
			String content = iter.next();
			map = mapper.readValue(content, Map.class);
			for (int i=0;i<filters.size(); i++) {
				ANode n = filters.get(i);
				boolean ok = n.test(map);
				if (ok) {
					add(map);
					for (int j=0;j<counters.size();j++) {
						Counter c = counters.get(j);
						c.process(map);			}
				}
			}			
		} 
		
		time = System.currentTimeMillis() - time;
		System.out.println("Read " + lines + " from " + input + " in " + time + " milliseconds");
		System.out.println("Result set contains " + this.size() + " records");
	}
	
	public void setFilter(String hierarchy, String op, Object value) throws Exception {
		ANode node = new ANode(hierarchy,hierarchy,op,value);
		filters.add(node);
	}
	
	public Counter setCounter(String hierarchy) throws Exception {
		List<String> h = new ArrayList();
		h.add(hierarchy);
		Counter c = new Counter(h,this);
		counters.add(c);
		return c;
	}
	
	public Counter setCounter(String... hierarchy) throws Exception {
		List<String> h = new ArrayList();
		for (String hh : hierarchy) {
			h.add(hh);
		}
		Counter c = new Counter(h,this);
		counters.add(c);
		return c;
	}
	
	public Counter setCounterUnique(String... hierarchy) throws Exception {
		List<String> h = new ArrayList();
		for (String hh : hierarchy) {
			h.add(hh);
		}
		CounterUnique c = new CounterUnique(h,this);
		counters.add(c);
		return c;
	}
	
	public Counter setCounter(List<String>h) throws Exception {
		Counter c = new Counter(h,this);
		counters.add(c);
		return c;
	}
	
	public Counter setCounter(List<String>h,String x) throws Exception {
		Counter c = new Counter(h,this);
		counters.add(c);
		return c;
	}
	
	
	public void clearFilters() {
		filters.clear();
	}
	
	public void clearCounters() {
		counters.clear();
	}
	
	public void report() {
		for (Counter c : counters) {
			c.report();
		}
	}
	
	public void report(int limit) {
		for (Counter c : counters) {
			c.report(limit);
		}
	}

	public Counter getCounter(String name) {
		for (Counter c : counters) {
			if (c.title.equals(name))
				return c;
		}
		return null;
	}
	
	public Counter getCounter(int i) {
		return counters.get(i);
	}
}
