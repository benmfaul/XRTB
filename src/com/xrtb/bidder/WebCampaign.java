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

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.google.gson.Gson;
import com.xrtb.commands.AddCampaign;
import com.xrtb.commands.DeleteCampaign;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.db.Database;
import com.xrtb.db.User;

/**
 * A Singleton class that handles all the campaigns.html actions. Basically it
 * serves up JSON data about campaigns, and can also load and unload campaigns.
 * 
 * @author Ben M. Faul
 *
 */
public class WebCampaign {
	Gson gson = new Gson();
	static WebCampaign instance;
	public Database db = new Database();

	private WebCampaign() {

	}

	/**
	 * Returns the singleton instance of the web campaign selector.
	 * 
	 * @return WebCampaign. The object that selects campaigns
	 */
	public static WebCampaign getInstance() {
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
		JsonNode rootNode = mapper.readTree(in);
		String data = rootNode.toString();

		Gson g = new Gson();
		Map r = new HashMap();
		Map m = g.fromJson(data, Map.class);
		String cmd = (String) m.get("command");
		if (cmd == null) {
			m.put("error", "No command given");
			m.put("original", data);
			return g.toJson(cmd);
		}

		if (cmd.equals("login")) {
			return doLogin(m);
		}

		if (cmd.equals("stub")) {
			return doNewCampaign(m);
		}

		if (cmd.equals("deletecampaign")) {
			return doDeleteCampaign(m);
		}
		if (cmd.equals("startcampaign")) {
			return startCampaign(m);
		}
		if (cmd.equals("stopcampaign")) {
			return stopCampaign(m);
		}
		m.put("error", "No such command: " + cmd);
		m.put("original", data);
		return g.toJson(cmd);
	}

	private String doLogin(Map m) {
		Map response = new HashMap();
		String message = null;
		String who = (String) m.get("username");

		if (who.equals("root")) {
			response.put("campaigns", db.getAllCampaigns());
			response.put("running",Configuration.getInstance().getLoadedCampaignNames());
			return gson.toJson(response);
		}

		User u = db.getUser(who);
		if (u == null) {
			try {
				db.createUser(who);
				db.write();
				message = "User " + who + " created";
			} catch (Exception error) {
				message = "Error: " + error.toString();
			}

		}
		response = getCampaigns(who);
		response.put("running",Configuration.getInstance().getLoadedCampaignNames());
		if (message != null)
			response.put("message", message);

		return gson.toJson(response);

	}

	private String doNewCampaign(Map m) {
		Map response = new HashMap();
		String who = (String) m.get("username");
		User u = db.getUser(who);
		if (u == null) {
			response.put("message", "No user " + who);
			return gson.toJson(response);
		}
		String name = (String) m.get("username");
		String id = (String) m.get("campaign");
		try {
			response.put("campaign", db.createStub(name, id));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response.put("message", "Error creating campaign: " + e.toString());
		}
		return gson.toJson(response);
	}

	private String doDeleteCampaign(Map m) throws Exception {
		Map response = new HashMap();
		String who = (String) m.get("username");
		String id = (String) m.get("campaign");
		User u = db.getUser(who);
		if (u == null) {
			response.put("message", "No user " + who);
			return gson.toJson(response);
		}
		response.put("campaigns", db.deleteCampaign(u, id));
		return gson.toJson(response);
	}

	public String startCampaign(Map cmd)  {
		Map response = new HashMap();
		try {
			String id = gson.toJson(cmd.get("id"));
			
			id = id.replaceAll("\"","");
			
			Campaign c = db.getCampaign(id);
			Controller.getInstance().addCampaign(c);
			response.put("error",false);
		} catch (Exception error) {
			response.put("message", "failed: " + error.toString());
			response.put("error", true);
		}
		response.put("running",Configuration.getInstance().getLoadedCampaignNames());
		return gson.toJson(response);
	}

	public String stopCampaign(Map cmd) {
		String adId = (String)cmd.get("id");
		Map response = new HashMap();
		try {
			Controller.getInstance().deleteCampaign(adId);
			response.put("error", false);
		} catch (Exception error) {
			response.put("message", "failed: " + error.toString());
			response.put("error", true);
		}
		response.put("running",Configuration.getInstance().getLoadedCampaignNames());
		return gson.toJson(response);
	}

	private Map getCampaigns(String who) {

		Map response = new HashMap();
		List camps = db.getCampaigns(who);

		response.put("campaigns", camps);
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
