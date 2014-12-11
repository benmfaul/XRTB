package com.xrtb.pojo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

/**
 * POJO for a bid object
 */
public class Bid {
	Map map = new HashMap();
	String json = "";
	ObjectMapper mapper = new ObjectMapper();
	
	public Bid(String id) {
		map.put("id", id);
	}
	
	public void put(String name, Object value) {
		map.put(name, value);
	}
	
	public Object get(String name) {
		return map.get(name);
	}
	
	public void del(String key) {
		map.remove(key);
	}
	
	public boolean isValid() {
		if (map.get("id") == null)
			return false;
		return true;
	}
	
	
	@Override
	public String toString() {	
		try {
			json = mapper.writeValueAsString(map);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}
}
