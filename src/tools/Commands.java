package tools;

import java.util.Scanner;

import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.core.MessageListener;
import org.redisson.core.RTopic;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.commands.AddCampaign;
import com.xrtb.commands.BasicCommand;
import com.xrtb.commands.DeleteCampaign;
import com.xrtb.commands.Echo;
import com.xrtb.commands.StartBidder;
import com.xrtb.commands.StopBidder;
import com.xrtb.db.Database;

/**
 * A simple class that sends and receives commands from RTB4FREE bidders.
 * @author Ben M. Faul
 *
 */

public class Commands {
	/** JSON object builder, in pretty print mode */
	
	/** The topic for commands */
	RTopic<BasicCommand> commands;
	/** The redisson backed shared map that represents this database */
	Redisson redisson;
	/** The redisson configuration object */
	Config cfg = new Config();
	static String redis;
	
	static Scanner scan = new Scanner(System.in);
	
/**
 * Main entry point, see description for usage.
 * @param args String[]. The array of arguments.
 */
 public static void main(String [] args) {
		redis = "localhost:6379";	
		int i = 0;
		Commands tool = null;
		if (args.length > 0) {
			while( i <args.length) {
				if (args[i].equals("-redis")) {
					redis = args[i+1];
					i+= 2;
				}
			}
		} else {
			tool = new Commands(redis);
		}
		scan = new Scanner(System.in);
		while(true) {
			System.out.print("RTB4FREE Commander (1=Echo, 2=Load into DB, 3=Delete from DB, 4=Stop Campaign, 5=Start Campaign, 6=Start Bidder, 7=Stop Bidder, 8=Exit Commander)\n??");
			String s = scan.nextLine();
			int num = Integer.parseInt(s);
			switch(num) {
			case 1:
				String to = scan.nextLine();
				tool.sendEcho(to);
				break;
			case 2:
				tool.loadDatabase();
				break;
			case 3:
				tool.removeUser();
				break;
			case 4:
				tool.stopCampaign();
				break;
			case 5:;
				tool.startCampaign();
				break;
			case 6:
				tool.startBidder();
				break;
			case 7:
				tool.stopBidder();
				break;
			case 8:
				tool.shutdown();
				System.out.println("Bye!");
				return;
			}
		}
 }
 
 /**
  * Instantiate a connection to localhost (Redisson)
  * Also contains the listener for responses.
  * @param redis String. The redis:host string.
  */
 public Commands(String redis) {
		cfg.useSingleServer()
    	.setAddress(redis)
    	.setConnectionPoolSize(10);
		redisson = Redisson.create(cfg);
     
     RTopic<BasicCommand> responses = redisson.getTopic("responses");
     responses.addListener(new MessageListener<BasicCommand>() {
         @Override
         public void onMessage(String channel,BasicCommand msg) {
        	 try {
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
     commands = redisson.getTopic("commands");
 }
 
 /**
  * Stop the redisson client.
  */
 public void shutdown() {
	 redisson.shutdown();
 }
 
 /**
  * Send an echo command
  * @param to. Whom to send the command to.
  */
 public void sendEcho(String to) {
	 Echo e = new Echo("Commander");
	 //e.to = to;
	 commands.publish(e);
 }
 
 /**
  * Send a stop bidder command
  */
 public void stopBidder() {
	 System.out.print("Which bidder to stop:");
	 String to = scan.nextLine();
	 StopBidder cmd = new StopBidder(to);
	 commands.publish(cmd);
 }
 
 /**
  * Send a start bidder command
  */
 public void startBidder() {
	 System.out.print("Which bidder to start:");
	 String to = scan.nextLine();
	 StartBidder cmd = new StartBidder(to);
	 commands.publish(cmd);
 }
 
 /**
  * Add more users to the redis database
  */
 public void loadDatabase() {
	 try {
		System.out.print("List of users to load:");
		String file = scan.nextLine();
		DbTools tool = new DbTools(redis);
		tool.loadDatabase(file);
	 } catch (Exception error) {
		 error.printStackTrace();
	 }
 }
 
 /**
  * Start a campaign (by loading into bidder memory
  */
 public void startCampaign() {
	 System.out.print("Which campaign to load:");
	 String cname = scan.nextLine();
	 System.out.print("Which bidder to notify");
	 String to = scan.nextLine();
	 AddCampaign cmd = new AddCampaign(to,cname);
	 commands.publish(cmd);
 }
 
 /**
  * Stop a campaign by removing from bidders memory
  */
 public void stopCampaign() {
	 System.out.print("Which campaign to stop:");
	 String cname = scan.nextLine();
	 System.out.print("Which bidder to notify:");
	 String to = scan.nextLine();
	 DeleteCampaign cmd = new DeleteCampaign(to,cname);
	 commands.publish(cmd); 
 }
 
 /**
  * Delete a user from the database 
  */
 public void removeUser() {
	 try {
		 System.out.println("Delete which user:");
			String user = scan.nextLine();
			DbTools tool = new DbTools(redis);
			tool.deleteUser(user);
		 } catch (Exception error) {
			 error.printStackTrace();
		 }
 }
 
 /**
  * Send a load from database file command.
  */
 public void loadInFromDatabase() {
	 System.out.print("Campaign id in database to load:");
	 String id = scan.nextLine();
	 AddCampaign cmd = new AddCampaign("",id);
	 commands.publish(cmd);
 }
 
 /**
  * Send a message to unload a campaign from memory.
  */
 public void unloadCampaignFromMemory() {
	 System.out.print("Campaign id to unload from memory:");
	 String id = scan.nextLine();
	 DeleteCampaign cmd = new DeleteCampaign("",id);
	 commands.publish(cmd);
 }
}
