package tools;

/**
 * A simple tool that prints a list of running bidders in the system (Within 30 second update window)
 * @author Ben M. Faul
 *
 */


public class ListRunningBidders {

	public static void main(String [] args) throws Exception {
		String host = "localhost";
		int port = 6379;
		
		int i = 0;
		while(i < args.length) {
			switch(args[i]) {
			case "-h":
				host = args[++i];
				i++;
				break;
			case "-p":
				port = Integer.parseInt(args[++i]);
				i++;
				break;
			default:
				System.err.println("Unknown: " + args[i]);
				return;
			}
		}
		MyNode x = new MyNode(host,port);
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

	public MyNode(String host, int port) throws Exception {
		super(host, port);	
	}
	
	@Override
	public void log(int level, String where, String msg) {
		
	}
	
}
