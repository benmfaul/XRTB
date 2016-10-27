package com.xrtb.tools.explorer;

import java.util.Map;
import java.util.Set;

public interface Interesting {
	public void setLimit(int l);
	public void process(Map m) throws Exception;
	public void report();
	public void report(int limit);
	public int size();
	public Set<String> keySet();
	public Object get(String key);
	public void clear();
	public String getTitle();
}
