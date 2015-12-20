package com.xrtb.tools;

import java.util.Scanner;


import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.RedissonClient;
import org.redisson.core.MessageListener;
import org.redisson.core.RTopic;

import com.xrtb.commands.BasicCommand;
import com.xrtb.commands.LogLevel;
import com.xrtb.common.Configuration;

/**
 * A simple class that sends a log change message to the rtb.
 * <p>
 * Usage: LogCommand [-redis host:port] [-level n] [-to 'instance-name']
 * </p>
 * <p>
 * Defaults: -redis localhost:6379 -level 2 -to '*'
 * @author Ben M. Faul
 *
 */

public class LogCommand {
	/** JSON object builder, in pretty print mode */
//	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	/** The topic for commands */
	RTopic<BasicCommand> commands;
	/** The redisson backed shared map that represents this database */
	RedissonClient redisson;
	/** The redisson configuration object */
	Config cfg = new Config();
	static String redis;
	
	static Scanner scan = new Scanner(System.in);
	
/**
 * Main entry point, see description for usage.
 * @param args String[]. The array of arguments.
 */
 public static void main(String [] args) throws Exception {
		redis = "localhost:6379";	
		int i = 0;
		String to = "*";
		LogCommand tool = null;
		String level = "2";
		if (args.length > 0) {
			while( i <args.length) {
				if (args[i].equals("-redis")) {
					redis = args[i+1];
					i+= 2;
				} else
				if (args[i].equals("-log")) {
					level = args[i+1];
					i+=2;
				} else
				if (args[i].equals("-to")) {
					to = args[i+1];
					i+=2;
				} else {
					System.err.println("Unknown directive: " + args[i]);
					System.exit(1);
				}
			}
		} 
		tool = new LogCommand(redis);
		tool.sendLogLevel(to,level);
		Thread.sleep(1000);
		tool.shutdown();
 }
 
 /**
  * Instantiate a connection to localhost (Redisson)
  * Also contains the listener for responses.
  * @param redis String. The redis:host string.
  */
 public LogCommand(String redis) throws Exception {
	 if (Configuration.setPassword() != null) {
		cfg.useSingleServer()
    	.setAddress(redis)
    	.setPassword(Configuration.setPassword())
    	.setConnectionPoolSize(10);
	 } else {
			cfg.useSingleServer()
	    	.setAddress(redis)
	    	.setConnectionPoolSize(10);
	 }
		redisson = Redisson.create(cfg);
     
     RTopic<BasicCommand> responses = redisson.getTopic("responses");
     responses.addListener(new MessageListener<BasicCommand>() {
         @Override
         public void onMessage(String channel, BasicCommand msg) {
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
 public void sendLogLevel(String to, String level) {
	 LogLevel e = new LogLevel(to,level);
	 //e.to = to;
	 commands.publish(e);
 }
}
