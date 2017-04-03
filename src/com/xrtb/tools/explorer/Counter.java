package com.xrtb.tools.explorer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class Counter implements Interesting {

	public List<ANode> nodes = new ArrayList();
	public int count;
	public Map<String, Integer> values = new HashMap();
	public String title = "";
	String sep = "";
	public int limit;

	public Counter(List<String> h) throws Exception {
		for (String hh : h) {
			ANode node = new ANode(hh, hh, "EQUALS", null);
			nodes.add(node);
		}
		title = h.toString();
	}

	public Counter() {

	}

	public void setSep(String str) {
		sep = str;
	}

	public void setLimit(int l) {
		limit = l;
	}

	public void process(Map m, CountDownLatch latch)  {
		Runnable updater = () -> {
			try {
				process(m);
				latch.countDown();
			} catch (Exception error) {

			}
		};
		Thread nthread = new Thread(updater);
		nthread.start();
	}

	public void process(Map m) throws Exception {
		Object result = null;
		String str = "";
		StringBuilder sb = new StringBuilder("");
		for (ANode n : nodes) {
			result = n.interrogate(0, m);
			if (result == null)
				return;
			sb.append(result.toString());
			sb.append(sep);
			// str += result.toString() + sep;
		}
		str = sb.toString();

		if (nodes.size() == 1)
			str = result.toString();

		if (sep.length() != 0) {
			str = str.substring(0, str.length() - sep.length());
		}

		Integer mK = values.get(str);
		if (mK == null) {
			mK = new Integer(0);
		}
		mK++;
		values.put(str, mK);
		count++;

	}

	public void report() {
		double ratio = ((double) count / (double) Anlz.size() * 100.0);
		System.out.printf("\n%s: %d(%.3f%%)", title, count, ratio);
		if (Anlz.addCr)
			System.out.println();
		List<Tuple> tups = Counter.reduce(values, count); // parent.size());
		int k = 0;
		if (limit == 0)
			limit = tups.size();
		for (Tuple q : tups) {
			if (k++ < limit) {
				if (Anlz.addCr)
					System.out.printf("%s, %d, (%.3f%%)\n", q.site, q.count, q.percent);
				else
					System.out.printf("%s, %d, (%.3f%%)", q.site, q.count, q.percent);
			} else
				return;
		}
		;
	}

	public void report(int limit) {
		this.limit = limit;
		report();
	}

	public static List<Tuple> reduce(Map data, int count) {
		Iterator itx = data.keySet().iterator();
		List<Tuple> tups = new ArrayList();
		Collections.sort(tups);
		while (itx.hasNext()) {
			String key = (String) itx.next();
			Integer value = (Integer) data.get(key);
			Tuple q = new Tuple(key, value, count);
			tups.add(q);
		}
		Collections.sort(tups);
		return tups;
	}

	public int size() {
		return values.size();
	}

	public Set<String> keySet() {
		return values.keySet();
	}

	public Object get(String key) {
		return values.get(key);
	}

	public void clear() {
		values.clear();
		limit = 0;
	}
	
	public String getTitle() {
		return title;
	}
}
