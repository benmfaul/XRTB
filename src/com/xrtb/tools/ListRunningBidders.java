package com.xrtb.tools;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.tools.NameNode;

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
		String host = "localhost";
		String pass = null;
		int port = 3000;
		boolean full = false;
		
		int i = 0;
		while(i < args.length) {
			switch(args[i]) {

			case "-h":
				System.out.println("-aero <hostname> [Sets the host for the cache to use]");
				System.out.println("-p <portnum>     [Sets the port of the cache        ]");
				System.out.println("-f               [Prints full status                ]");
				System.exit(1);;
				
			case "-aero":
				host = args[++i];
				i++;
				break;
			case "-p":
				port = Integer.parseInt(args[++i]);
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
		MyNode x = new MyNode(host,port,pass);
		if (full) {
			for (String name : x.getMembers()) {
				System.out.println("Bidder: " + name);
				Map u = x.getMemberStatus(name);
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

	public MyNode(String host, int port, String pass) throws Exception {
		super(host, port);	
	}
	
	@Override
	public void log(int level, String where, String msg) {
		
	}
	
}
