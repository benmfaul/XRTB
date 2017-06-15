package test.java;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.openrtb.OpenRtb.BidRequest;
import com.google.openrtb.json.OpenRtbJsonFactory;
import com.google.openrtb.json.OpenRtbJsonReader;
import com.xrtb.bidder.Controller;
import com.xrtb.bidder.RTBServer;
import com.xrtb.blocks.NavMap;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.HttpPostGet;
import com.xrtb.db.DataBaseObject;
import com.xrtb.db.User;
import com.xrtb.exchanges.adx.AdxBidRequest;
import com.xrtb.exchanges.adx.AdxBidResponse;
import com.xrtb.exchanges.google.GoogleBidRequest;
import com.xrtb.exchanges.google.GoogleBidResponse;
import com.xrtb.fraud.ForensiqClient;
import com.xrtb.fraud.FraudLog;
import com.xrtb.tools.DbTools;

/**
 * A class for testing that the bid has the right parameters
 * 
 * @author Ben M. Faul
 *
 */
public class TestGoogle {
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
	//@Test
	public void testGoogleProtobufBanner() throws Exception {
		HttpPostGet http = new HttpPostGet();
		GoogleBidRequest google = GoogleBidRequest.fromRTBFile("./SampleBids/nexage.txt");
		byte[] protobytes = google.getInternal().toByteArray();
		byte[] returns = http.sendPost("http://" + Config.testHost + "/rtb/bids/google", protobytes, 300000, 300000);
		int code = http.getResponseCode();
		assertTrue(code == 200);
		assertNotNull(returns);
		GoogleBidResponse rr = new GoogleBidResponse(returns);
		System.out.println(rr.getInternal());

	}

	//@Test
	public void testGoogleProtobufVideo() throws Exception {
		HttpPostGet http = new HttpPostGet();
		GoogleBidRequest google = GoogleBidRequest.fromRTBFile("./SampleBids/nexage.txt");
		byte[] protobytes = google.getInternal().toByteArray();
		byte[] returns = http.sendPost("http://" + Config.testHost + "/rtb/bids/google", protobytes, 300000, 300000);
		int code = http.getResponseCode();
		assertTrue(code == 200);
		assertNotNull(returns);
		GoogleBidResponse rr = new GoogleBidResponse(returns);
		System.out.println(rr.getInternal());

	}
	
	@Test
	public void testGoogleDecrypt() throws Exception {
		String payload = "http://localhost:8080/rtb/win/google/WP5SPgAE9TEKDFtHAAnnOm9LuUuqG14LOdRXXQ/0.0/0.0/55/87/WPfq6wABDYsKUaXKwgwIUw";
		HttpPostGet http = new HttpPostGet();
		http.sendGet(payload,300000,300000);
	}
}

class MyReader extends OpenRtbJsonReader {

	protected MyReader(OpenRtbJsonFactory factory) {
		super(factory);
		// TODO Auto-generated constructor stub
	}

}
