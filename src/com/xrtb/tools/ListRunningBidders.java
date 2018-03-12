package com.xrtb.tools;

import com.xrtb.RedissonClient;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * A simple tool that prints a list of running bidders in the system (Within 30 second update window)
 * @author Ben M. Faul
 *
 */


public class ListRunningBidders {
	
	public static ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public static void main(String [] args) throws Exception {
		String pub = "tcp://localhost:2000";
		String sub = "tcp://localhost:2001";
		boolean full = false;
		
		int i = 0;
		while(i < args.length) {
			switch(args[i]) {

			case "-h":
				System.out.println("-p publish-endpoint [Sets publisher endpoint]");
				System.out.println("-s subscribe-endpoint    [Sets the subscriber endpoint       ]");
				System.out.println("-f               [Prints full status                ]");
				System.exit(1);;
				
			case "-p":
				pub = args[++i];
				i++;
				break;
			case "-s":
				sub = args[++i];
				i++;
				break;
			case "-f":
				full = true;
				i++;
				break;
			default:
				System.err.println("Unknown: " + args[i]);
				return;
			}
		}
		RedissonClient client = new RedissonClient();
		client.setSharedObject(pub,sub);
		MyNode x = new MyNode(client);
		if (full) {
			for (String name : x.getMembers()) {
				System.out.println("Bidder: " + name);
				Map u = x.getStatus(name);
				System.out.println(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(u));
				System.out.println("-----------------------------------------");
			}
		} else 
			System.out.println("Running Bidders: " + x.getMembers());
		x.stop();

	}
}

/**
 * Don't use the logger, just override the name node.
 * @author Ben M. Faul
 *
 */
class MyNode extends NameNode {

	public MyNode(RedissonClient client) throws Exception {
		super(client);
	}
	
	@Override
	public void log(int level, String where, String msg) {
		
	}
	
}
