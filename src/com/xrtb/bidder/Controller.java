package com.xrtb.bidder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;

/**
 * A class for handling REDIS based commands to the RTB server.
 * @author Ben M. Faul
 *
 */
public class Controller {
	public static final int ADD_CAMPAIGN = 0;
	public static final int DEL_CAMPAIGN = 1;
	public static final int STOP_BIDDER = 2;
	public static final int START_BIDDER = 3;
	public static final int PERCENTAGE = 4;
	public static final int ECHO = 5;
	
	public static final String COMMANDS = "commands";
	public static final String RESPONSES = "responses";
	public static final String PUBLISH = "publish";
	Jedis commands;
	Jedis publish;
	Jedis responses;
	
	CommandLoop loop;
	Publisher responseQueue;
	Publisher publishQueue;
	Set<Campaign> campaigns = new TreeSet<Campaign>();
	static Controller theInstance;

	/**
	 * Private default constructor, uses localhost
	 */
	private Controller() {
		List<String> list = new ArrayList();
		list.add("localhost");
		list.add("localhost");
		Map<String,String> m = new HashMap();
		m.put(PUBLISH, "localhost");
		m.put(RESPONSES,"localhost");
		m.put(COMMANDS, "localhost");
		setup(m);
	}

	/**
	 * Private construcotr with specified hosts
	 * @param hosts. Map. Used to describe COMMANDS, PUBLISH and RESPONSES hosts
	 * used by the REDIS controller.
	 */
	private Controller(Map<String, String> hosts) {
		setup(hosts);
	}
	
	/**
	 * Set up the REDIS connections.
	 * @param hosts. Map used to describe COMMANDS, PUBLISH and RESPONSE hosts
	 * used by the Redis controller.
	 */
	void setup(Map<String,String> hosts) {
		publish = new Jedis(hosts.get(PUBLISH));
		publish.connect();
		
		responses = new Jedis(hosts.get(RESPONSES));
		responses.connect();

		commands = new Jedis(hosts.get(COMMANDS));
		commands.connect();
		loop = new CommandLoop(commands);
		
		responseQueue = new Publisher(responses,RESPONSES);
		publishQueue = new Publisher(publish,PUBLISH);
	}

	/**
	 * Get the controller using localhost for REDIS connections.
	 * @return Controller. The singleton object of the controller.
	 */
	public static Controller getInstance() {
		if (theInstance == null) {
			synchronized (Controller.class) {
				if (theInstance == null) {
					Map<String, String> hosts = new HashMap();
					hosts.put(COMMANDS, "localhost");
					hosts.put(PUBLISH, "localhost");
					hosts.put(RESPONSES, "localhost");
					theInstance = new Controller(hosts);
				}
			}
		}
		return theInstance;
	}

	/**
	 * Get the controller using the Mapped names for REDIS connections.
	 * @return Controller. The singleton object of the controller.
	 * @param hosts. Map used to describe COMMANDS, PUBLISH and RESPONSE hosts
	 * @return Controller. The singleton object of the controller.
	 */
	public static Controller getInstance(Map hosts) {
		if (theInstance == null) {
			synchronized (CampaignSelector.class) {
				if (theInstance == null) {
					theInstance = new Controller(hosts);
				}
			}
		}
		return theInstance;
	}

	/**
	 * TODO: Make a runner for this.
	 * @param control String. The name of the queue.
	 * @param message String. The message from this queue.
	 */
	public synchronized void sendMessage(String control, String message) {
		synchronized(theInstance) {
			if (control.equals(RESPONSES))
				responseQueue.add(message);	

			if (control.equals(PUBLISH))
				publishQueue.add(message);
		}	
	}

	/**
	 * Add a campaign over REDIS.
	 * @param node. JsonNode -JSON of command.
	 */
	public void addCampaign(JsonNode node) {
		responseQueue.add("Response goes here");
	}

	/**
	 * Delete a campaign using REDIS.
	 * @param node. JsonNode - JSON of command.
	 */
	public void deleteCampaign(JsonNode node) {
		responseQueue.add("Response goes here");
	}

	/**
	 * Stop the bidder.
	 * @param node. JsonNode - JSON of command.
	 */
	public void stopBidder(JsonNode node) {
		responseQueue.add("Response goes here");
	}

	/**
	 * Start the bidder.
	 * @param node. JsonNode - the JSON of the command.
	 */
	public void startBidder(JsonNode node) {
		responseQueue.add("Response goes here");
	}

	/**
	 * Set the throttle percentage.
	 * @param node. JsoNode - JSON  of the command.
	 */
	public void setPercentage(JsonNode node) {
		responseQueue.add("Response goes here");
	}
	
	/**
	 * THe echo command and its response.
	 * @param echo. Map. The echo command.
	 * @throws Exception. Throws Exception on REDIS errors.
	 */
	public void echo(Map<String,Object> echo) throws Exception  {
		Map m = RTBServer.getStatus();
		echo.put("msg",m);
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = mapper.writeValueAsString(echo);
		responseQueue.add(jsonString);
	}
	
	/*
	 * The not handled response to the command entity. Used when an
	 * unrecognized command is sent.
	 * @param echo. Map - the error message to send.
	 */
	public void notHandled(Map<String,Object> echo) throws Exception  {
		Map m = RTBServer.getStatus();
		echo.put("msg","error, unhandled event");
		echo.put("status", "error");
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = mapper.writeValueAsString(echo);
		responseQueue.add(jsonString);
	}
}

/**
 * A class to retrieve RTBServer commands from REDIS.
 * @author Ben M. Faul
 *
 */
class CommandLoop extends JedisPubSub implements Runnable {
	Thread me;
	Jedis conn;

	/**
	 * Constructor.
	 * @param conn. Jedis - the Jedis connection dedicated to receiving
	 * commands.
	 */
	public CommandLoop(Jedis conn) {
		this.conn = conn;
		me = new Thread(this);
		me.start();
	}

	/**
	 * Subscribes the Jedis commands queue, does not return.
	 */
	public void run() {
		conn.subscribe(this, Controller.COMMANDS);
	}

	/**
	 * On a message from REDIS, handle the command.
	 * @param arg0. String - the channel of this message.
	 * @param arg1. String - the JSON encoded message.
	 */
	@Override
	public void onMessage(String arg0, String arg1) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = null;
			rootNode = mapper.readTree(arg1);
			JsonNode node = rootNode.path("cmd");
			int command = node.getIntValue();
			
			Map<String, Object> mapObject = mapper.readValue(rootNode, 
					new TypeReference<Map<String, Object>>(){});
			mapObject.put("from", Configuration.instanceName);
			
			switch(command) {
			case Controller.ADD_CAMPAIGN:
				Controller.getInstance().addCampaign(rootNode);
				break;
			case Controller.DEL_CAMPAIGN:
				Controller.getInstance().deleteCampaign(rootNode);
				break;
			case Controller.STOP_BIDDER:
				Controller.getInstance().stopBidder(rootNode);
				break;
			case Controller.START_BIDDER:
				Controller.getInstance().startBidder(rootNode);
				break;
			case Controller.PERCENTAGE:
				Controller.getInstance().setPercentage(rootNode);
				break;
			case Controller.ECHO:
				Controller.getInstance().echo(mapObject);
				break;
			default:
				Controller.getInstance().notHandled(mapObject);
			}
				
		} catch (Exception error) {
			Controller.getInstance().responseQueue.add(error.toString());
			error.printStackTrace();
		}
	}

	@Override
	public void onPMessage(String arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPSubscribe(String arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPUnsubscribe(String arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSubscribe(String arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUnsubscribe(String arg0, int arg1) {
		// TODO Auto-generated method stub

	}
}

/**
 * A publisher for REDIS based messages, sharable by multiple threads.
 * @author Ben M. Faul
 *
 */
class Publisher implements Runnable {
	Thread me;
	Jedis conn;
	String channel;
	List<String> msgs = new ArrayList();

	public Publisher(Jedis conn, String channel) {
		this.conn = conn;
		this.channel = channel;
		me = new Thread(this);
		me.start();
	}

	/**
	 * Run the message pump.
	 */
	public void run() {
		while(true) {
			try {
				Thread.sleep(1);
				if (msgs.size()>0) {
					String message = msgs.get(0);
					msgs.remove(0);
					conn.publish(channel, message);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
		}
	}

	/**
	 * Add a message to the messages queue.
	 * @param s. String. JSON formatted message.
	 */
	public void add(String s) {
		msgs.add(s);
	}
}

