package tools;

import java.util.Scanner;
import java.util.concurrent.ConcurrentMap;

import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.core.MessageListener;
import org.redisson.core.RTopic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xrtb.commands.AddCampaign;
import com.xrtb.commands.BasicCommand;
import com.xrtb.commands.ClickLog;
import com.xrtb.commands.DeleteCampaign;
import com.xrtb.commands.Echo;
import com.xrtb.commands.PixelClickConvertLog;
import com.xrtb.commands.StartBidder;
import com.xrtb.commands.StopBidder;
import com.xrtb.db.User;

/**
 * A simple class that watches for clicks, conversions and pixels from the bidders.
 * Arguments:
 * <p>
 * [-redis host:port]		Sets the redis host/port, uses localhost as default
 * <p>
 * [-watch pixels|clicks|conversions] What to watch for
 * <p>
 * eg java -jar xrtb.jar tools.WatchPixelClickConvert pixels <<< to watch pixel loads
 * <p>
 * By default, no arguments means connect to localhost, watch for pixels, clicks and conversions
 * @author Ben M. Faul
 *
 */

public class WatchPixelClickConvert {
	/** JSON object builder, in pretty print mode */
	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	/** The topic for commands */
	RTopic<BasicCommand> commands;
	/** The redisson backed shared map that represents this database */
	Redisson redisson;
	/** The redisson configuration object */
	Config cfg = new Config();
	/** which to watch, click, convert or pixel, or all? */
	int watch;
	
	/**
	 * The main program entry.
	 * @param args String [] args. See the description for usage.
	 */
 public static void main(String [] args) {
		String redis = "localhost:6379";	
		String channel = "clicks";
		int what = -1;
		int i = 0;
		if (args.length > 0) {
			while( i <args.length) {
				if (args[i].equals("-redis")) {
					redis = args[i+1];
					i+= 2;
				}
				if (args[i].equals("-channel")) {
					channel = args[i+1];
					i+=2;
				}
				if (args[i].equals("-watch")) {
					String str = args[i+1];
					if (str.equals("clicks"))
						what = PixelClickConvertLog.CLICK;
					if (str.equals("pixels"))
						what = PixelClickConvertLog.PIXEL;
					if (str.equals("conversions"))
						what = PixelClickConvertLog.CONVERT;
					System.out.println("Watching: " + str);
					i+= 2;
				}
			}
		}
		new WatchPixelClickConvert(redis,channel, what);
 }
 
 /**
  * Instantiate a connection to localhost (Redisson)
  * Also contains the listener for the pixels, clicks and conversions.
  */
 public WatchPixelClickConvert(String redis, String channel, int what) {
		cfg.useSingleServer()
    	.setAddress(redis)
    	.setConnectionPoolSize(10);
		redisson = Redisson.create(cfg);
     
	 watch = what;
     RTopic<PixelClickConvertLog> responses = redisson.getTopic(channel);
     responses.addListener(new MessageListener<PixelClickConvertLog>() {
         @Override
         public void onMessage(PixelClickConvertLog msg) {
        	 if (watch == -1 || msg.type == watch)
        		 System.out.println(gson.toJson(msg));
         }
     });
 }
}
