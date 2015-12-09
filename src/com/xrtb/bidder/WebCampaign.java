package com.xrtb.bidder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.eclipse.jetty.server.Request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.xrtb.commands.AddCampaign;
import com.xrtb.commands.DeleteCampaign;
import com.xrtb.commands.LogLevel;
import com.xrtb.commands.StartBidder;
import com.xrtb.commands.StopBidder;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.HttpPostGet;
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

	public String handler(HttpServletRequest request, InputStream in) throws Exception {
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
			return doLogin(request,m);
		}
		
		if (cmd.equals("loginAdmin")) {
			return getAdmin(m);
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
		if (cmd.equals("updatecampaign")) {
			return updateCampaign(m);
		}
		if (cmd.equals("deletefile")) {
			return doDeleteFile(m);
		}
		
		if (cmd.equalsIgnoreCase("executeCommand")) {
			return doExecute(m);
		}
		
		m.put("error", true);
		m.put("message","No such command: " + cmd);
		m.put("original", data);
		return g.toJson(cmd);
	}

	private String doLogin(HttpServletRequest request, Map m) throws Exception {
		Map response = new HashMap();
		String message = null;
		String who = (String) m.get("username");
		String pass = (String)m.get("password");

		if (who.equals("root")) {
			response.put("campaigns", db.getAllCampaigns());
			response.put("running",Configuration.getInstance().getLoadedCampaignNames());

			Controller.getInstance().sendLog(3, "WebAccess-Login",
					"root user has logged in");
			return gson.toJson(response);
		}

		User u = db.getUser(who);

		if (u == null) {
			response.put("error", true);
			response.put("message", "No such login");
			Controller.getInstance().sendLog(3, "WebAccess-Login",
					"Bad login:" + who);
			return gson.toJson(response);
		}
		
		Controller.getInstance().sendLog(3, "WebAccess-Login",
				"User has logged in: " + who);
		
		response = getCampaigns(who);
		response.put("username", who);
		response.put("running",Configuration.getInstance().getLoadedCampaignNames());
		
		HttpSession session = request.getSession();
		session.setAttribute("user", u.name);
		
		try {
			File f = new File(u.directory);
		    File [] paths = f.listFiles();
	        List files = new ArrayList();
	        Map locator = null;
	        
	         // for each pathname in pathname array
	         for(File path:paths)
	         {
	        	 locator = new HashMap();
	        	 locator.put("uri",u.directory+"/"+path.getName());
	        	 locator.put("name",path.getName());
	        	 files.add(locator);
	         }
	         
			 response.put("images", files);
		} catch (Exception error) {
			//error.printStackTrace();
			Controller.getInstance().sendLog(3, "WebAccess-doLogin","Error, initializing user data, problem: " + error.toString());
		//	response.put("error",true);             // we are going to allow it, no local files.
		}
		response.put("images", getFiles(u));
		if (message != null)
			response.put("message", message);

		return gson.toJson(response);

	}
	
	public String multiPart( Request baseRequest,
			HttpServletRequest request, MultipartConfigElement config) throws Exception  {
		
		HttpSession session = request.getSession(false);
		String user = (String)session.getAttribute("user");
		User u = db.getUser(user);
		if (u == null)
			throw new Exception("No such user");
		
		baseRequest.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, config);
		Collection<Part> parts = request.getParts();
		for (Part part : parts) {
			System.out.println("" + part.getName());
		}

		Part filePart = request.getPart("file");

		InputStream imageStream = filePart.getInputStream();
		byte[] resultBuff = new byte[0];
	    byte[] buff = new byte[1024];
	    int k = -1;
	    while((k = imageStream.read(buff, 0, buff.length)) > -1) {
	        byte[] tbuff = new byte[resultBuff.length + k]; // temp buffer size = bytes already read + bytes last read
	        System.arraycopy(resultBuff, 0, tbuff, 0, resultBuff.length); // copy previous bytes
	        System.arraycopy(buff, 0, tbuff, resultBuff.length, k);  // copy current lot
	        resultBuff = tbuff; // call the temp buffer as your result buff
	    }
	    System.out.println(resultBuff.length + " bytes read.");
		
		if (k == 0) {		// no file provided
			throw new Exception("No file provided");
		} else {
			byte [] bytes = new byte[1024];
			Part namePart = request.getPart("name");
			InputStream nameStream = namePart.getInputStream();
			int rc = nameStream.read(bytes);
			String name = new String(bytes,0,rc);
			FileOutputStream fos = new FileOutputStream(u.directory + "/" + name);
			fos.write(resultBuff);
			fos.close();
		}
		Map response = new HashMap();
		response.put("images",getFiles(u));
		return gson.toJson(response);
	
	}
	
	private List getFiles(User u) {
        List files = new ArrayList();
		try {
			File f = new File(u.directory);
		    File [] paths = f.listFiles();
	        Map locator = null;
	        
	         // for each pathname in pathname array
	         for(File path:paths)
	         {
	        	 locator = new HashMap();
	        	 locator.put("uri",u.directory+"/"+path.getName());
	        	 locator.put("name",path.getName());
	        	 files.add(locator);
	         }
	         
		} catch (Exception error) {
			System.err.println("User " + u.name + " han an error accessing his files");
			try {
				Controller.getInstance().sendLog(3, "WebAccess-getFiles",
						"User " + u.name + " han an error accessing his files");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//error.printStackTrace();
		}
		return files;
	}

	private String doNewCampaign(Map m) throws Exception {
		Map response = new HashMap();
		String who = (String) m.get("username");
		User u = db.getUser(who);
		if (u == null) {
			response.put("message", "No user " + who);
			return gson.toJson(response);
		}
		String name = (String) m.get("username");
		String id = (String) m.get("campaign");
		
		Controller.getInstance().sendLog(3, "WebAccess-New-Campaign",
				who + " added a new campaign: " + id);
		
		try {
			if (db.getCampaign(name,id) != null) {
				response.put("error",true);
				response.put("message", "Error, campaign by that name is already defined");
				return gson.toJson(response);
			}
			Campaign c = db.createStub(name,id);
			db.editCampaign(name, c);
			response.put("campaign", c);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response.put("error",true);
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
		Controller.getInstance().sendLog(3, "WebAccess-New-Campaign",
				who + " deleted a campaign " + id);
		
		response.put("campaigns", db.deleteCampaign(u, id));
		return gson.toJson(response);
	}
	
	public String doDeleteFile(Map m) throws Exception {
		Map response = new HashMap();
		String who = (String) m.get("username");
		String filename = (String) m.get("file");
		User u = db.getUser(who);
		if (u == null) {
			response.put("message", "No user " + who);
			return gson.toJson(response);
		}
		
		String fname = u.directory + "/" + filename;
		File f = new File(fname);
		f.delete();
		
		Controller.getInstance().sendLog(3, "WebAccess-New-Campaign",
				who + " deleted a file " + fname); 
		response.put("images",getFiles(u));
		return gson.toJson(response);
	}

	/**
	 * Starts the campaign from the web portal
	 * @param cmd Map. The JSON command structure from the web user.
	 * @return String. The JSON string of all the running campaigns in this bidder.
	 */
	public String startCampaign(Map cmd) throws Exception  {
		Map response = new HashMap();
		try {
			String id = gson.toJson(cmd.get("id"));
			String name = (String)cmd.get("username");
			
			id = id.replaceAll("\"","");
			
			Campaign c = db.getCampaign(name,id);
			Controller.getInstance().addCampaign(c);
			response.put("error",false);
			
			AddCampaign command = new AddCampaign(null,name,id);
			command.to = "*";
			command.from = Configuration.getInstance().instanceName;
			
			Controller.getInstance().commandsQueue.add(command);
			
			Controller.getInstance().sendLog(3, "WebAccess-Start-Campaign",
					"Campaign start: " + id);
			
		} catch (Exception error) {
			response.put("message", "failed: " + error.toString());
			response.put("error", true);
		}
		response.put("running",Configuration.getInstance().getLoadedCampaignNames());
		return gson.toJson(response);
	}
	
	/**
	 * Updates a command in the database (NOT in the currently running list)
	 * @param cmd Map. The web user command map.
	 * @return String. JSON representation of the running campaigns.
	 */
	public String updateCampaign(Map cmd) {
		Map response = new HashMap();
		try {
			String name = (String)cmd.get("username");
			String id = gson.toJson(cmd.get("id"));
			
			id = id.replaceAll("\"","");
			String data = (String)cmd.get("campaign");
			
			Campaign c = new Campaign(data);
			
			db.editCampaign(name, c);
			response.put("error",false);
			
			AddCampaign command = new AddCampaign(null,name,id);
			command.to = "*";
			command.from = Configuration.getInstance().instanceName;
			
			Controller.getInstance().commandsQueue.add(command);
			
			Controller.getInstance().sendLog(3, "WebAccess-Update-Campaign",
					name + " Modified campaign: " + id);
		} catch (Exception error) {
			response.put("message", "failed: " + error.toString());
			response.put("error", true);
		}
		response.put("running",Configuration.getInstance().getLoadedCampaignNames());
		return gson.toJson(response);
	}

	/**
	 * Delete a campaign
	 * @param cmd Map. The delete command map from the web user.
	 * @return String. The list of campaigns running.
	 */
	public String stopCampaign(Map cmd) {
		String name = (String)cmd.get("username");
		String adId = (String)cmd.get("id");
		Map response = new HashMap();
		try {
			Controller.getInstance().deleteCampaign(name,adId);
			response.put("error", false);
			DeleteCampaign command = new DeleteCampaign(name,adId);
			command.to = "*";
			command.from = Configuration.getInstance().instanceName;
			
			Controller.getInstance().commandsQueue.add(command);;
			
			Controller.getInstance().sendLog(3, "WebAccess-New-Campaign",
				"Campaign stopped:  " + adId);
		} catch (Exception error) {
			response.put("message", "failed: " + error.toString());
			response.put("error", true);
		}
		response.put("running",Configuration.getInstance().getLoadedCampaignNames());
		return gson.toJson(response);
	}

	/**
	 * Return a map off all the campaigns in the database for the specified user.
	 * @param who String. The user name.
	 * @return Map. A response map containing campaigns.
	 */
	private Map getCampaigns(String who) throws Exception {

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
	
	///////////////////////////////////////////////////////////
	
	public String getAdmin(Map cmd) {
		Map m = new HashMap();
		try {
			List<String> userList = db.getUserList();
			List<Map> users = new ArrayList();
			for (String s : userList) {
				Map x = new HashMap();
				User u = db.getUser(s);
				x.put("name", u.name);
				users.add(x);
			}
			
			m.put("initials", Configuration.getInstance().initialLoadlist);
			m.put("seats", Configuration.getInstance().seatsList);
			m.put("users", users);
			m.put("status", getStatus());
			m.put("summaries", getSummary());
		} catch (Exception error) {
			m.put("error", true);
			m.put("message",error.toString());
		}
		return gson.toJson(m);
	}
	
	public String doExecute(Map m) throws Exception {
		String action = (String)m.get("action");
		String who = (String)m.get("who");
		String username = (String)m.get("username");
		switch (action) {
		case "start":
			StartBidder start = new StartBidder();
			start.from = username;
			start.to = who;
			Controller.getInstance().startBidder(start);
			break;
		case "stop":
			StopBidder stop = new StopBidder();
			stop.from = username;
			stop.to = who;
			Controller.getInstance().stopBidder(stop);
			break;
		case "loglevel":
			LogLevel level = new LogLevel();
			level.from = username;
			level.to = who;
			String valu = (String)m.get("level");
			level.target = valu;
			Controller.getInstance().setLogLevel(level);
			break;
		case "reload":
			break;
		default:
			break;
		}
		
		Map x = new HashMap();
		x.put("message","Command sent");
		return gson.toJson(x);
	}
	
	private List getSummary() throws Exception {
		String data = null;
		List core = new ArrayList();
		
		List<String> members = RTBServer.node.getMembers();
		for (String member : members) {
			Map entry = new HashMap();
			HttpPostGet http = new HttpPostGet();
			Map values = new HashMap();
			if (member.equals(Configuration.getInstance().instanceName)) {
				values.put("stopped",RTBServer.stopped);
				values.put("ncampaigns",Configuration.getInstance().campaignsList.size());
				values.put("loglevel",Configuration.getInstance().logLevel);
			} else {
				String [] parts = member.split(":");
				String port = parts[parts.length-1];
				String url = parts[1] + ":" + port + "/summary";
				String rc = http.sendGet(url);
				if (rc != null) {
					Map info = gson.fromJson(rc, Map.class);
					values.put("stopped",info.get("stopped"));
					values.put("ncampaigns",info.get("ncampaigns"));
					values.put("loglevel",info.get("loglevel"));
				}
			}
			entry.put("name", member);
			entry.put("values", values);
			core.add(entry);
		}
		return core;
	}
	
	private List getStatus() throws Exception {
		String data = null;
		List core = new ArrayList();
		
		List<String> members = RTBServer.node.getMembers();
		for (String member : members) {
			Map entry = new HashMap();
			HttpPostGet http = new HttpPostGet();
			Map values = new HashMap();
			if (member.equals(Configuration.getInstance().instanceName)) {
				RTBServer.getStatus();
				values.put("total",RTBServer.handled);
				values.put("bid", RTBServer.bid);
				values.put("nobid",RTBServer.nobid);
				values.put("win",RTBServer.win);
				values.put("clicks",RTBServer.clicks);
				values.put("pixels",RTBServer.pixels);
				values.put("errors",RTBServer.error);
				values.put("adspend", RTBServer.adspend);
				values.put("qps", RTBServer.qps);
				values.put("avgx", RTBServer.avgx);
			} else {
				String [] parts = member.split(":");
				String port = parts[parts.length-1];
				String url = parts[1] + ":" + port + "/info";
				String rc = http.sendGet(url);
				if (rc != null) {
					Map info = gson.fromJson(rc, Map.class);
					values.put("total",info.get("handled"));
					values.put("bid",info.get("bid"));
					values.put("nobid",info.get("nobid"));
					values.put("win",info.get("win"));
					values.put("clicks",info.get("clicks"));
					values.put("pixels",info.get("pixels"));
					values.put("errors",info.get("error"));
					values.put("adspend", info.get("adspend"));
					values.put("qps", info.get("qps"));
					values.put("avgx", info.get("avgx"));
				}
			}
			entry.put("name", member);
			entry.put("values", values);
			core.add(entry);
		}
		
		return core;
		
	}
}
