package test.java;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.jboss.netty.handler.ipfilter.CIDR;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.bidder.Controller;
import com.xrtb.bidder.RTBServer;
import com.xrtb.blocks.NavMap;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.ForensiqLog;
import com.xrtb.common.HttpPostGet;
import com.xrtb.db.DataBaseObject;
import com.xrtb.db.User;
import com.xrtb.exchanges.adx.AdxBidRequest;
import com.xrtb.exchanges.adx.AdxBidResponse;
import com.xrtb.pojo.ForensiqClient;
import com.xrtb.tools.DbTools;

/**
 * A class for testing that the bid has the right parameters
 * 
 * @author Ben M. Faul
 *
 */
public class TestAdx {
	public static final String testHost = "localhost:8080";
	static final String redisHost = "localhost3000";
	static DbTools tools;
	/** The RTBServer object used in the tests. */
	static RTBServer server;

	@BeforeClass
	public static void testSetup() {
		try {
			DbTools tools = new DbTools("localhost:3000");
			tools.clear();
			tools.loadDatabase("database.json");

			if (server == null) {
				server = new RTBServer("./Campaigns/payday.json");
				int wait = 0;
				while (!server.isReady() && wait < 10) {
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

	@AfterClass
	public static void testCleanup() {
		Configuration c = Configuration.getInstance();
		c.campaignsList.clear();
	}

	/**
	 * Test a valid bid response.
	 * 
	 * @throws Exception
	 *             on networking errors.
	 */
	@Test
	public void testAdx() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader("SampleBids/adxrequests"));
		String data;
		ObjectMapper mapper = new ObjectMapper();
		HttpPostGet http = new HttpPostGet();
		while ((data = br.readLine()) != null) {
			Map map = mapper.readValue(data, Map.class);
			String protobuf = (String) map.get("protobuf");
			if (protobuf != null) {
				byte[] protobytes = DatatypeConverter.parseBase64Binary(protobuf);
				InputStream is = new ByteArrayInputStream(protobytes);
				byte [] returns = http.sendPost("http://" + Config.testHost + "/rtb/bids/adx", protobytes);
				// AdxBidResponse resp = new AdxBidResponse(returns);
				// System.out.println(resp.toString());
			/*	try {
					AdxBidRequest bidRequest = new AdxBidRequest(is);
					System.out.println(bidRequest.internal);
					System.out.println("============================================");
					System.out.println(bidRequest.root);
					System.out.println("--------------------------------------------");
				} catch (Exception error) {
error.printStackTrace();
				} */
			}
		}

	}
}
