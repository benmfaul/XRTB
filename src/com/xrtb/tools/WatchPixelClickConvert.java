package com.xrtb.tools;

import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.RedissonClient;
import org.redisson.core.MessageListener;
import org.redisson.core.RTopic;

import com.xrtb.commands.BasicCommand;
import com.xrtb.commands.ClickLog;
import com.xrtb.commands.DeleteCampaign;
import com.xrtb.commands.Echo;
import com.xrtb.commands.PixelClickConvertLog;
import com.xrtb.commands.StartBidder;
import com.xrtb.commands.StopBidder;
import com.xrtb.common.Configuration;
import com.xrtb.db.Database;
import com.xrtb.db.User;
import com.xrtb.pojo.BidResponse;
import com.xrtb.pojo.WinObject;

/**
 * A simple class that watches for clicks, conversions and pixels from the
 * bidders. Arguments:
 * <p>
 * [-redis host:port] Sets the redis host/port, uses localhost as default
 * <p>
 * [-watch pixels|clicks|conversions] What to watch for
 * <p>
 * eg java -jar xrtb.jar tools.WatchPixelClickConvert pixels - to watch pixel
 * loads
 * <p>
 * By default, no arguments means connect to localhost, watch for pixels, clicks
 * and conversions
 * 
 * @author Ben M. Faul
 *
 */

public class WatchPixelClickConvert {
	/** The topic for commands */
	RTopic<BasicCommand> commands;

	/** The redisson backed shared map that represents this database */

	/**
	 * The main program entry.
	 * 
	 * @param args
	 *            String [] args. See the description for usage.
	 */
	public static void main(String[] args) throws Exception {
		String redis = "localhost:6379";
		String channel = "clicks";
		int what = PixelClickConvertLog.CLICK;

		int i = 0;
			while (i < args.length) {
				if (args[i].equals("-h")) {
					System.out
							.println("-redis <host:port>     [Set the host port to use]");
					System.out
							.println("-channel <channelname> [The channel to use: bids, wins, clicks, default is clicks");
					System.out
							.println("-watch                 [pixel|clicks|conversions, use when channel is clicks default is clicks]");
					System.exit(1);
				} else
				if (args[i].equals("-redis")) {
					redis = args[i + 1];
					i += 2;
				} else
				if (args[i].equals("-channel")) {
					channel = args[i + 1];
					i += 2;
					if (channel.equals("bids"))
						what = -1;
					if (channel.equals("wins"))
						what = -2;
				} else
				if (args[i].equals("-watch")) {
					String str = args[i + 1];
					if (str.equals("clicks"))
						what = PixelClickConvertLog.CLICK;
					if (str.equals("pixels"))
						what = PixelClickConvertLog.PIXEL;
					if (str.equals("conversions"))
						what = PixelClickConvertLog.CONVERT;
					
					System.out.println("Watching: " + str);
					i += 2;
				}
			}
		if (what > 0)
			new PixelClickConvert(redis, channel, what);
		if (what == -1) 
			new BidWatch(redis,channel);
		if (what == -2) 
			new WinWatch(redis,channel);
	}
}

/**
 * Instantiate a connection to localhost (Redisson) Also contains the listener
 * for the pixels, clicks and conversions.
 * 
 * @param redis
 *            String. The redis host:port string.
 * @param channel
 *            String. The topic of what we are looking for.
 * @param what
 *            int. The integer type of what we are looking for.
 */

class PixelClickConvert {
	RedissonClient redisson;
	/** The redisson configuration object */
	Config cfg = new Config();
	/** which to watch, click, convert or pixel, or all? */
	int watch;

	public PixelClickConvert(String redis, String channel, int what)
			throws Exception {
		if (Configuration.setPassword() != null) {
			cfg.useSingleServer().setAddress(redis)
					.setPassword(Configuration.setPassword())
					.setConnectionPoolSize(10);
		} else {
			cfg.useSingleServer().setAddress(redis).setConnectionPoolSize(10);
		}
		redisson = Redisson.create(cfg);

		watch = what;
		RTopic<PixelClickConvertLog> responses = redisson.getTopic(channel);
		responses.addListener(new MessageListener<PixelClickConvertLog>() {
			@Override
			public void onMessage(String channel, PixelClickConvertLog msg) {
				if (watch == -1 || msg.type == watch) {
					try {
						String content = DbTools.mapper.writer()
								.withDefaultPrettyPrinter()
								.writeValueAsString(msg);
						System.out.println(content);
					} catch (Exception error) {
						error.printStackTrace();
					}
				}
			}
		});
	}
}

/**
 * Instantiate a connection to localhost (Redisson) Also contains the listener
 * for the pixels, clicks and conversions.
 * 
 * @param redis
 *            String. The redis host:port string.
 * @param channel
 *            String. The topic of what we are looking for.
 * @param what
 *            int. The integer type of what we are looking for.
 */

class BidWatch {
	RedissonClient redisson;
	/** The redisson configuration object */
	Config cfg = new Config();
	/** which to watch, click, convert or pixel, or all? */
	int watch;

	public BidWatch(String redis, String channel) throws Exception {
		if (Configuration.setPassword() != null) {
			cfg.useSingleServer().setAddress(redis)
					.setPassword(Configuration.setPassword())
					.setConnectionPoolSize(10);
		} else {
			cfg.useSingleServer().setAddress(redis).setConnectionPoolSize(10);
		}
		redisson = Redisson.create(cfg);

		RTopic<BidResponse> responses = redisson.getTopic(channel);
		responses.addListener(new MessageListener<BidResponse>() {
			@Override
			public void onMessage(String channel, BidResponse msg) {
				try {
					String content = DbTools.mapper.writer()
							.withDefaultPrettyPrinter().writeValueAsString(msg);
					System.out.println(content);
				} catch (Exception error) {
					error.printStackTrace();
				}
			}
		});
	}
}

class WinWatch {
	RedissonClient redisson;
	/** The redisson configuration object */
	Config cfg = new Config();
	/** which to watch, click, convert or pixel, or all? */
	int watch;

	public WinWatch(String redis, String channel) throws Exception {
		if (Configuration.setPassword() != null) {
			cfg.useSingleServer().setAddress(redis)
					.setPassword(Configuration.setPassword())
					.setConnectionPoolSize(10);
		} else {
			cfg.useSingleServer().setAddress(redis).setConnectionPoolSize(10);
		}
		redisson = Redisson.create(cfg);

		RTopic<WinObject> responses = redisson.getTopic(channel);
		responses.addListener(new MessageListener<WinObject>() {
			@Override
			public void onMessage(String channel, WinObject msg) {
				try {
					String content = DbTools.mapper.writer()
							.withDefaultPrettyPrinter().writeValueAsString(msg);
					System.out.println(content);
				} catch (Exception error) {
					error.printStackTrace();
				}
			}
		});
	}
}
