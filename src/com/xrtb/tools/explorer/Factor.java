package com.xrtb.tools.explorer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class to factor all the parts of the requests being processed.
 * @author Ben M. Faul
 *
 */
public class Factor extends Counter {

	Set<String> factors = new HashSet();
	
	public Factor() {
		
	}
	
	/**
	 * Process the bid request (as a map).
	 */
	public void process(Map m) throws Exception {
		Set set = m.keySet();
		Iterator it = set.iterator();
		List<String> names = new ArrayList();
		names.add("request");
		while(it.hasNext()) {
			String key = (String)it.next();
			Object obx = m.get(key);
			
			List<String> anames = new ArrayList();
			anames.addAll(names);
			anames.add(key);
			doit(obx,anames);
		}
	}
	
	/**
	 * Print the report
	 */
	public void report() {
		System.out.printf("\n%s",title);
		List<String> list = new ArrayList();
		for (String s : factors) {
		    list.add(s);
		}
		
		Collections.sort(list);
		for (String s : list) {
		    System.out.println(s);
		}
		
	}
	
	/**
	 * Walk the object map.
	 * @param obj Object. The object to walk.
	 * @param names List. The names encountered along the way.
	 */
	public void doit(Object obj, List<String> names) {
		if (obj instanceof Map) {
			Map m = (Map)obj;
			Set set = m.keySet();
			Iterator it = set.iterator();
			while(it.hasNext()) {
				String key = (String)it.next();
				Object obx = m.get(key);
				
				List<String> anames = new ArrayList();
				anames.addAll(names);
				anames.add(key);
				doit(obx,anames);
			}
			return;
		} else
		if (obj instanceof List) {
			List list = (List)obj;
			int k = 0;
			if (list.size() == 0) {
				String name  = makeName(names);
				name = name + ", List, unknown";
				return;
			}
			Object test = list.get(0);
			if (test instanceof Map == false) {
				String name  = makeName(names);
				name += ", List, ";
				if (test instanceof Integer) {
					name += "Integer";
				} else
				if (test instanceof Double) {
					name += "Double";
				} else
				if (test instanceof String) {
					name += "String";
				}
				factors.add(name);
				return;
			}
			for (Object obx : list) {			
				List<String> anames = new ArrayList();
				anames.addAll(names);
				anames.add(Integer.toString(k));
				k++;
				doit(obx,anames);
			}
		} else
		if (obj instanceof Integer) {
		    String name = makeName(names);
		    name = name + ", Scalar, Integer";
			factors.add(name);
		}
		else
		if (obj instanceof Double) {
			String name = makeName(names);
			name = name + ", Scalar, Double";
			factors.add(name);
		}
		else
		if (obj instanceof String) {
			String name = makeName(names);
			name += ", Scalar, String";
			factors.add(name);
		}
	}
	
	/**
	 * Create a name from the list like: a.b.c.d
	 * @param names List. The list of names walking the hierarchy.
	 * @return String. The name hierarchy in dotted form.
	 */
	String makeName(List<String> names) {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<names.size();i++) {
			String n = names.get(i);
			sb.append(n);
			if (i < names.size() - 1) {
				sb.append(".");
			}
		}
		return sb.toString();
	}
}
