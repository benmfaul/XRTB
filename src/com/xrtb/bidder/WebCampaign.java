package com.xrtb.bidder;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.gson.Gson;
import com.xrtb.common.Configuration;

/**
 * A Singleton class that handles all the campaigns.html actions. Basically it serves up
 * JSON data about campaigns, and can also load and unload campaigns.
 * @author Ben M. Faul
 *
 */
public class WebCampaign {
	Gson gson = new Gson();
	
	static WebCampaign instance;
	
	private WebCampaign() {
		
	}
	
	/**
	 * Returns the singleton instance of the web campaign selector.
	 * 
	 * @return WebCampaign. The object that selects campaigns
	 */
	public static WebCampaign getInstance()  {
		if (instance == null) {
			synchronized (WebCampaign.class) {
				if (instance == null) {
					instance = new WebCampaign();
				}
			}
		}
		return instance;
	}
	
	public String handler(InputStream in) throws Exception {
		/**
		 * Use jackson to read the content, then use gson to turn it into a map.
		 */
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode  = mapper.readTree(in);
		String data = rootNode.toString();

		Gson g = new Gson();
		Map r = new HashMap();
		Map m = g.fromJson(data, Map.class);
		String cmd = (String)m.get("command");
		if (cmd == null) {
			m.put("error", "No command given");
			m.put("original", data);
			return g.toJson(cmd);
		}
		
		if (cmd.equals("login")) {
			return doLogin(m);
		}
		m.put("error", "No such command: " + cmd);
		m.put("original", data);
		return g.toJson(cmd);
	}

	private String doLogin(Map m) {
		Map response = new HashMap();
		String who = (String)m.get("username");
		return gson.toJson(getCampaigns(who));
		
	}
	
	private Map getCampaigns(String who) {
		Map response = new HashMap();
		List camps = new ArrayList();
		Map mx = new HashMap();
		mx.put("name", "Test 1");
		mx.put("value", "Number 1");
		camps.add(mx);
		
		mx = new HashMap();
		mx.put("name", "Test 2");
		mx.put("value", "Number 2");
		camps.add(mx);
		
		mx = new HashMap();
		mx.put("value", "Number 3");
		mx.put("name", "Test 3");
		camps.add(mx);
		
		String code = "<ul class='list-group'>\n";
		for (int i=0;i<camps.size();i++) {
			mx = (Map)camps.get(i);
			String name = (String)mx.get("name");
			String value = (String)mx.get("value");
			code += "     <a class='list-group-item' onclick='doCamp(\"" + value + "\")'>" + name + "</a>\n";
		}
		code += "\n</ul>\n";
		
	   code += "<ul class='nav nav-tabs'>\n";
       code += "<li class='active'><a onclick='alert(\"home\")'>Home</a></li>\n";
       code += "<li><a onclick='alert(\"profile\")'>Profile</a></li>\n";
       code += "<li><a onclick='alert(\"messages\")'>messages</a></li>\n";
       code += "</ul>\n";
		
		response.put("campaigns", camps);
		response.put("code", code);
		response.put("message", "User created");
		return response;
	}
	
	public String getCampaign(String id) {
		return null;
	}
	
	public String getActiveCampaigns() {
		return null;
	}
	
	public String deactivateCampaign(String id) {
		return null;
	}
	
	public String activateCampaign(String id) {
		return null;
	}
	
	public String saveCampaign(String campaign) {
		return null;
	}
	
	public String removeCampaign(String campaign) {
		return null;
	}
}
