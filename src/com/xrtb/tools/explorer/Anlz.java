package com.xrtb.tools.explorer;

import java.io.File;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Anlz  {

	String input;
	long lines;
	static ArrayList data = new ArrayList();
	static List<ANode> filters = new ArrayList();
	static List<Interesting> counters = new ArrayList();
	static boolean addCr;
	boolean keep = false;
	public static ObjectMapper mapper = new ObjectMapper();
	static int limit = -1;
	int count = 0;
	static boolean print = false;
	
	// -r imp.0.banner.w 320 -r imp.0.banner.h 350

	public static void main(String[] args) throws Exception {
		String fileName = null;
		int i = 0;
		while(i < args.length) {
			switch(args[i]) {
			case "-h":
			case "-help":
				System.out.println("-h                     This message");
				System.out.println("-f filename            Filename to anlyz");
				System.out.println("-r rtbspec OP value    Sets a fileter");
				System.exit(0);;
				break;
			case "-f":
				fileName = args[i+1];
				i+=2;
				break;
			case "-r":
				String spec = args[i+1];
				String op = args[i+2];
				String value = args[i+3];
				Integer x = null;
				try {
					x = Integer.parseInt(value);
					setFilter(spec, op, x);
				} catch (Exception error) {
					setFilter(spec, op, value);
				}
				i+=4;
				break;
			case "-l":
				limit = Integer.parseInt(args[i+1]);
				i+= 2;
				break;
			case "-p":
				print = true;
				i++;
				break;
			case "-c":
				setCounter(args[i+1]);
				i+=2;
				break;
			}
		}
		Anlz a = new Anlz(fileName);
		a.addCr = true;
		a.standard();
	}

	public static int size() {
		return data.size();
	}

    public void keep(boolean t) {
		keep = t;
	}

	public void standard() throws Exception {
		
		setFilter("bcat", "NOT_MEMBER", "XXX");

		List<String> h = new ArrayList();
		setCounter("imp.0.banner.w", "imp.0.banner.h").setSep("x");
		setCounterUnique("imp.0.banner.battr");
		setCounterUnique("imp.0.banner.mimes");
		setAverage("imp.0.bidfloor");
	//	setCounter("site.domain");
		setCounterUnique("bcat");
	//	setCounterUnique("site.cat");
	//	setCounter("app.domain");
		setCounter("device.os");
		setCounter("device.make");
//		setCounter("device.geo.country");
		
		counters.add(new Factor());
		
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

	public Anlz() throws Exception {
		System.out.println("Anlz 1.0");
	}
	
	public Anlz(String fileName) throws Exception {
		setInput(fileName);
	}

	public void process() throws Exception {
		Map map = null;
		long time = System.currentTimeMillis();

		Stream<String> stream = Files.lines(Paths.get(input));
		Iterator<String> iter = stream.iterator();
		while (iter.hasNext()) {
			lines++;
			if (limit != -1 && count < limit == false)
				break;
			String content = iter.next();
			map = mapper.readValue(content, Map.class);
			boolean ok = false;
			for (int i = 0; i < filters.size(); i++) {
				ANode n = filters.get(i);
				ok = n.test(map);
				if (!ok)
					break;
			}
			if (ok) {
				if (keep)
					data.add(map);
				for (int j = 0; j < counters.size(); j++) {
					Interesting c = counters.get(j);
					c.process(map);
				}
				if (print) {
					System.out.println("---------------" + content + "-------------------");
				}
				count++;
				if (count % 1000 == 0) {
					System.out.println("... " + count);
				}
			}
			
		}

		time = System.currentTimeMillis() - time;
		System.out.println("Read " + lines + " from " + input + " in " + time + " milliseconds");
		System.out.println("Result set contains " + this.size() + " records");
	}

	public static void setFilter(String hierarchy, String op, Object value) throws Exception {
		ANode node = new ANode(hierarchy, hierarchy, op, value);
		filters.add(node);
	}

	public static Counter setCounter(String hierarchy) throws Exception {
		List<String> h = new ArrayList();
		h.add(hierarchy);
		Counter c = new Counter(h);
		counters.add(c);
		return c;
	}

	public static Counter setCounter(String... hierarchy) throws Exception {
		List<String> h = new ArrayList();
		for (String hh : hierarchy) {
			h.add(hh);
		}
		Counter c = new Counter(h);
		counters.add(c);
		return c;
	}

	public static Counter setCounterUnique(String... hierarchy) throws Exception {
		List<String> h = new ArrayList();
		for (String hh : hierarchy) {
			h.add(hh);
		}
		CounterUnique c = new CounterUnique(h);
		counters.add(c);
		return c;
	}
	
	public static  Average setAverage(String h) throws Exception {
		Average a = new Average(h);
		counters.add(a);
		return a;
	}

	public static Counter setCounter(List<String> h) throws Exception {
		Counter c = new Counter(h);
		counters.add(c);
		return c;
	}

	public static Counter setCounter(List<String> h, String x) throws Exception {
		Counter c = new Counter(h);
		counters.add(c);
		return c;
	}

	public static void clearFilters() {
		filters.clear();
	}

	public static void clearCounters() {
		counters.clear();
	}

	public void report() {
		for (Interesting c : counters) {
			c.report();
		}
	}

	public void report(int limit) {
		for (Interesting c : counters) {
			c.report(limit);
		}
	}

	public Interesting getCounter(String name) {
		for (Interesting c : counters) {
			if (c.getTitle().equals(name))
				return c;
		}
		return null;
	}

	public Interesting getCounter(int i) {
		return counters.get(i);
	}
}
