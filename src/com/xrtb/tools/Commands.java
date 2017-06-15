package com.xrtb.tools;

import java.util.Scanner;



import java.util.UUID;

import com.xrtb.bidder.ZPublisher;
import com.xrtb.commands.AddCampaign;
import com.xrtb.commands.BasicCommand;
import com.xrtb.commands.DeleteCampaign;
import com.xrtb.commands.Echo;
import com.xrtb.commands.StartBidder;
import com.xrtb.commands.StopBidder;
import com.xrtb.jmq.MessageListener;
import com.xrtb.jmq.RTopic;

/**
 * A simple class that sends and receives commands from RTB4FREE bidders.
 * @author Ben M. Faul
 *
 */

public class Commands {
	/** JSON object builder, in pretty print mode */
	
	/** The topic for commands */
	ZPublisher commands;
	
	static String aerospike;

	public static String uuid = UUID.randomUUID().toString();
	static String password = null;
	
	static Scanner scan = new Scanner(System.in);
	
/**
 * Main entry point, see description for usage.
 * @param args String[]. The array of arguments.
 */
 public static void main(String [] args) throws Exception {
		aerospike = "localhost:3000";	
		int i = 0;
		Commands tool = null;
		if (args.length > 0) {
			while( i <args.length) {
				if (args[i].equals("-aerospike")) {
					aerospike = args[i+1];
					i+= 2;
				} else
				if (args[i].equals("-auth")) {
					password = args[i+1];
					i+=2;
				}
			}
		} else {
			tool = new Commands(aerospike);
		}
		scan = new Scanner(System.in);
		while(true) {
			System.out.print("RTB4FREE Commander\n" +
					"(1=Echo, 2=Load JSON Database into Aerospike, 3=Delete Campaign from Aerospike,\n" +
					"4=Stop Campaign, 5=Start Campaign, 6=Start Bidder, 7=Stop Bidder,\n" + 
					"8=Exit Commander)\n??");
			String s = scan.nextLine();
			try {
			switch(s) {
			case "1":
				System.out.print("who:");
				String to = scan.nextLine();
				tool.sendEcho(to);
				break;
			case "2":
				tool.loadDatabase();
				break;
			case "3":
				tool.deleteCampaign();
				break;
			case "4":
				tool.stopCampaign();
				break;
			case "5":
				tool.startCampaign();
				break;
			case "6":
				tool.startBidder();
				break;
			case "7":
				tool.stopBidder();
				break;
			case "8":
				tool.shutdown();
				System.out.println("Bye!");
				return;
			default:
				System.out.println("I didn't understand that...");
			} } catch (Exception error) {
				System.out.println("Error: " + error.toString());
			}
		}
 }
 
 /**
  * Instantiate a connection to localhost (Redisson)
  * Also contains the listener for responses.
  * @param aero String. The aerospike:host string.
  */
 public Commands(String aero) throws Exception {
     
     RTopic responses = new RTopic("tcp://*:5575&responses");
     responses.addListener(new MessageListener<BasicCommand>() {
         @Override
         public void onMessage(String channel,BasicCommand msg) {
        	 try {
        		 
        	if (msg.to.equals(uuid) == false)
        		return;
        		 
        	 String content = DbTools.mapper
        				.writer()
        				.withDefaultPrettyPrinter()
        				.writeValueAsString(msg);
             System.out.println("<<<<<" + content);
             System.out.print("??");
        	 } catch (Exception error) {
        		 error.printStackTrace();
        	 }
         }
     });
     commands = new ZPublisher("tcp://*:5580", "commands");
 }
 
 /**
  * Stop the redisson client.
  */
 public void shutdown() {

 }
 
 /**
  * Send an echo command
  * @param to. Whom to send the command to.
  */
 public void sendEcho(String to) {
	 Echo e = new Echo();
	 e.from = uuid;
	 if (!(to.length() == 0 || to.equals("*")))
			 e.to = to;
	 commands.add(e);
 }
 
 /**
  * Send a stop bidder command
  */
 public void stopBidder() {
	 System.out.print("Which bidder to stop:");
	 String to = scan.nextLine();
	 StopBidder cmd = new StopBidder(to);
	 cmd.from = uuid;
	 commands.add(cmd);
 }
 
 /**
  * Send a start bidder command
  */
 public void startBidder() {
	 System.out.print("Which bidder to start:");
	 String to = scan.nextLine();
	 StartBidder cmd = new StartBidder(to);
	 cmd.from = uuid;
	 commands.add(cmd);
 }
 
 /**
  * Add more users to the redis database
  */
 public void loadDatabase() {
	 try {
		System.out.print("Filename of database to load into Aerospike (not the bidders):");
		String file = scan.nextLine();
		DbTools tool = new DbTools(aerospike);
		tool.loadDatabase(file);
		
	 } catch (Exception error) {
		 error.printStackTrace();
	 }
 }
 
 public void deleteCampaign() throws Exception{
		System.out.print("Campaign to Delete from Aerospike:");
		String adid = scan.nextLine();
		DbTools tool = new DbTools(aerospike);
		tool.deleteCampaign(adid);
		System.out.println("Ok, campaign deleted");
 }
 
 /**
  * Start a campaign (by loading into bidder memory
  */
 public void startCampaign() {
	 System.out.print("Which username:");
	 String name = scan.nextLine();
	 System.out.print("Which campaign to load from Aerospike:");
	 String cname = scan.nextLine();
	 System.out.print("Which bidder to load campaign into:");
	 String to = scan.nextLine();
	 AddCampaign cmd = new AddCampaign(to,name,cname);
	 cmd.from = uuid;
	 commands.add(cmd);
 }
 
 /**
  * Stop a campaign by removing from bidders memory
  */
 public void stopCampaign() {
	 System.out.print("Which bidder to stop campaign:");
	 String to = scan.nextLine();
	 System.out.print("Which campaign owner: ");
	 String owner = scan.nextLine();
	 System.out.print("Which campaign: ");
	 String cname = scan.nextLine();
	 DeleteCampaign cmd = new DeleteCampaign(to,owner,cname);
	 cmd.from = uuid;
	 commands.add(cmd); 
 }
 
 /**
  * Send a load from database file command.
  */
 public void loadInFromDatabase() {
	 System.out.print("Username in database to load:");
	 String name = scan.nextLine();
	 System.out.print("Campaign id in database to load:");
	 String id = scan.nextLine();
	 AddCampaign cmd = new AddCampaign("",name,id);
	 cmd.from = uuid;
	 commands.add(cmd);
 }
 

}
