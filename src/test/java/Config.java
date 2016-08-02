package test.java;

import static org.junit.Assert.fail;

import com.xrtb.bidder.RTBServer;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.db.DataBaseObject;
import com.xrtb.db.User;
import com.xrtb.tools.DbTools;

/**
 * The JUNIT common configuration is done here. Start the server if not running. If it is running, then
 * reload the campaigns from REDIS as tests can monmkey with them.
 * 
 * @author Ben M. Faul
 *
 */
public class Config {
	/** The hostname the test programs will use for the RTB bidder */
	public static final String testHost = "localhost:8080";
	/** The RTBServer object used in the tests. */
	static RTBServer server;

	public static void setup() throws Exception {
		try {
			DbTools tools = new DbTools("localhost:6379");
			tools.clear();
			tools.loadDatabase("database.json");
			
			if (server == null) {	
				server = new RTBServer("./Campaigns/payday.json");
				int wait = 0;
				while(!server.isReady() && wait < 10) {
					Thread.sleep(1000);
					wait++;
				}
				if (wait == 10) {
					fail("Server never started");
				}
				Thread.sleep(1000);
			} else {
				Configuration c = Configuration.getInstance();
				c.campaignsList.clear();
				User u = DataBaseObject.getInstance().get("ben");
				for (Campaign camp : u.campaigns) {
					c.addCampaign("ben", camp.adId);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}
	
	/** 
	 * JUNIT Test configuration for shards.
	 * 
	 */
	public static void setup(String shard, int port) throws Exception {
		try {
			if (server == null) {
				DbTools tools = new DbTools("localhost:6379");
				tools.clear();
				tools.loadDatabase("database.json");
				server = new RTBServer("./Campaigns/payday.json", shard, port);
				int wait = 0;
				while(!server.isReady() && wait < 10) {
					Thread.sleep(1000);
					wait++;
				}
				if (wait == 10) {
					fail("Server never started");
				}
			} else {
				Configuration c = Configuration.getInstance();
				c.campaignsList.clear();
				User u = DataBaseObject.getInstance().get("ben");
				for (Campaign camp : u.campaigns) {
					c.addCampaign("ben", camp.adId);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	public static void teardown() {
//		if (server != null) {
//			server.halt();
//		}
		Configuration c = Configuration.getInstance();
		c.campaignsList.clear();
	}
}
