package tools;

import org.redisson.Redisson;
import org.redisson.core.MessageListener;
import org.redisson.core.RTopic;

import com.xrtb.commands.BasicCommand;
import com.xrtb.commands.Echo;

/**
 * A simple class that sends and receives commands from RTB4FREE bidders.
 * @author Ben M. Faul
 *
 */

public class Commands {
	/** The topic for commands */
	RTopic<BasicCommand> commands;
	/** The redisson object */
	Redisson redisson;
	
 public static void main(String [] args) {
	 Commands c = new Commands();
	 c.sendEcho();
 }
 
 /**
  * Instantiate a connection to localhost (Redisson)
  * Also contains the listener for responses.
  */
 public Commands() {
	 Redisson redisson = Redisson.create();
     commands = redisson.getTopic("commands");
     
     RTopic<BasicCommand> responses = redisson.getTopic("responses");
    responses.addListener(new MessageListener<BasicCommand>() {
         @Override
         public void onMessage(BasicCommand msg) {
             System.out.println("<<<<<" + msg);
         }
     });
 }
 
 /**
  * Send an echo command
  */
 public void sendEcho() {
	 Echo e = new Echo();
	 System.out.println(">>>>> " + e);
	 commands.publish(e);
 }

}
