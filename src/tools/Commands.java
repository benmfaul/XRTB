package tools;

import java.util.Scanner;

import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.core.MessageListener;
import org.redisson.core.RTopic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xrtb.commands.AddCampaign;
import com.xrtb.commands.BasicCommand;
import com.xrtb.commands.DeleteCampaign;
import com.xrtb.commands.Echo;
import com.xrtb.commands.StartBidder;
import com.xrtb.commands.StopBidder;

/**
 * A simple class that sends and receives commands from RTB4FREE bidders.
 * @author Ben M. Faul
 *
 */

public class Commands {
	/** JSON object builder, in pretty print mode */
	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	/** The topic for commands */
	RTopic<BasicCommand> commands;
	/** The redisson backed shared map that represents this database */
	Redisson redisson;
	/** The redisson configuration object */
	Config cfg = new Config();
	
	static Scanner scan = new Scanner(System.in);
	
 public static void main(String [] args) {
		String redis = "localhost:6379";	
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
		System.out.println("RTB4FREE Commander (1=Echo, 2=Load into DB, 3=Delete from DB, 4=Stop Campaign, 5=Start Campaign, 6=Start Bidder, 7=Stop Bidder, 8=Exit Commander)");
		scan = new Scanner(System.in);
		while(true) {
			System.out.print("??");
			int num = scan.nextInt();
			switch(num) {
			case 1:
				String to = scan.nextLine();
				tool.sendEcho(to);
			case 2:
				String file = scan.nextLine();
				//tool.loadDatabase();
			case 3:
			case 4:
			case 5:
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
  */
 public Commands(String redis) {
		cfg.useSingleServer()
    	.setAddress(redis)
    	.setConnectionPoolSize(10);
		redisson = Redisson.create(cfg);
     
     RTopic<BasicCommand> responses = redisson.getTopic("responses");
     responses.addListener(new MessageListener<BasicCommand>() {
         @Override
         public void onMessage(BasicCommand msg) {
             System.out.println("<<<<<" + gson.toJson(msg));
             System.out.print("??");
         }
     });
     commands = redisson.getTopic("commands");
 }
 
 public void shutdown() {
	 redisson.shutdown();
 }
 
 /**
  * Send an echo command
  */
 public void sendEcho(String to) {
	 Echo e = new Echo("Commander");
	 //e.to = to;
	 commands.publish(e);
 }
 
 public void stopBidder() {
	 System.out.print("Which bidder to stop:");
	 String to = scan.nextLine();
	 StopBidder cmd = new StopBidder(to);
	 commands.publish(cmd);
 }
 
 public void startBidder() {
	 System.out.print("Which bidder to stop:");
	 String to = scan.nextLine();
	 StartBidder cmd = new StartBidder(to);
	 commands.publish(cmd);
 }
 
 public void loadInFromDatabase() {
	 System.out.print("Campaign id in database to load:");
	 String id = scan.nextLine();
	 AddCampaign cmd = new AddCampaign("",id);
	 commands.publish(cmd);
 }
 
 public void unloadCampaignFromMemory() {
	 System.out.print("Campaign id to unload from memory:");
	 String id = scan.nextLine();
	 DeleteCampaign cmd = new DeleteCampaign("",id);
	 commands.publish(cmd);
 }

}
