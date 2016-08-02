package com.xrtb.tools;

import com.xrtb.tools.NameNode;

/**
 * A simple tool that prints a list of running bidders in the system (Within 30 second update window)
 * @author Ben M. Faul
 *
 */


public class ListRunningBidders {

	public static void main(String [] args) throws Exception {
		String host = "localhost";
		String pass = null;
		int port = 6379;
		
		int i = 0;
		while(i < args.length) {
			switch(args[i]) {

			case "-h":
				System.out.println("-redis <hostname> [Sets the host for the cache to use]");
				System.out.println("-port <portnum>   [Sets the port of the cache]");
				System.out.println("-a <password>     [Sets the cache password to use]");
				System.exit(1);;
				
			case "-redis":
				host = args[++i];
				i++;
				break;
			case "-p":
				port = Integer.parseInt(args[++i]);
				i++;
				break;
			case "-a":
				pass = args[i+1];
				i+=2;
				break;
			default:
				System.err.println("Unknown: " + args[i]);
				return;
			}
		}
		MyNode x = new MyNode(host,port,pass);
		System.out.println("Running RTB4FREE bidders: " + x.getMembers());
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
		super(host, port, pass);	
	}
	
	@Override
	public void log(int level, String where, String msg) {
		
	}
	
}
