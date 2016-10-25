package com.xrtb.tools.explorer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CounterUnique extends Counter {

	public CounterUnique(List<String> h, Anlz parent) throws Exception {
		super();
		this.parent = parent;

		for (String hh : h) {
			ANode node = new ANode(hh, hh, "MEMBER", null);
			nodes.add(node);
		}

		title = h.toString();
	}

	public CounterUnique(List<String> h) throws Exception {
		super();
		for (String hh : h) {
			ANode node = new ANode(hh, hh, "MEMBER", null);
			nodes.add(node);
		}
		title = h.toString();
	}

	public void process(Map m) throws Exception {
		Object result = null;
		String str = "";
		StringBuilder sb = new StringBuilder("");
		for (ANode n : nodes) {
			result = n.interrogate(0, m);
			if (result == null)
				return;
			if (result instanceof ArrayList) {
				List x = (List) result;
				if (x.size()==0) {
					return;
				}
				Object y = x.get(0);
				if (y instanceof String) {
					List<String> list = (List) result;

					for (String s : list) {
						Integer mK = values.get(s);
						if (mK == null) {
							mK = new Integer(0);
						}
						mK++;
						values.put(s, mK);
					}
				} else {
					List<Integer> list = (List) result;

					for (Integer s : list) {
						Integer mK = values.get(s.toString());
						if (mK == null) {
							mK = new Integer(0);
						}
						mK++;
						values.put(s.toString(), mK);
					}
				}
			}
		}
		count++;
	}
}
