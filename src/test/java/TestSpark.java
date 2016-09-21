package test.java;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.RedissonClient;
import org.redisson.core.RTopic;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xrtb.bidder.Controller;
import com.xrtb.bidder.WebCampaign;
import com.xrtb.commands.PixelClickConvertLog;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.HttpPostGet;
import com.xrtb.common.Node;
import com.xrtb.jmq.XPublisher;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;
import com.xrtb.pojo.WinObject;
import com.xrtb.tools.logmaster.Spark;

/**
 * Test Geo fencing
 * 
 * @author Ben M. Faul
 *
 */

public class TestSpark {

	@BeforeClass
	public static void setup() {
		System.out.println("******************  TestNode");
	}
	
	@Test
	public void testNoAuth() {
		try {
			Spark sp = new Spark("localhost:6379", null, "localhost", true);
			fail("Should have failed auth is required");
		} catch (Exception error) {

		}
	}

	@Test
	public void testAuth() {
		try {
			Spark sp = new Spark("localhost:6379", "startrekisbetterthanstarwars", "localhost", true);
			transmit("startrekisbetterthanstarwars");
			Thread.sleep(1000);
			
			long bids = sp.bids.get();
			long wins = sp.wins.get();
			long cost = sp.winCost.get();
			long price = sp.bidCost.get();
			assertTrue(bids == 1000);
			assertTrue(wins == 1000);
			assertTrue(cost == 1000);
			assertTrue(price == 1000);
		} catch (Exception error) {
			fail("Auth should have worked here");
		}
	}

	public void transmit(String pass) throws Exception {
		String crid = "111";

		WinObject obj = new WinObject();
		obj.cost = ".001";
		obj.price = ".001";
		obj.adId = "123";
		obj.cridId = crid;

		BidResponse br = new BidResponse();
		br.adid = "123";
		br.crid = crid;
		br.cost = .001;

		PixelClickConvertLog cmd = new PixelClickConvertLog();
		cmd.ad_id = "123";
		cmd.creative_id = crid;


		XPublisher  wins = new XPublisher("tcp://*:5572","wins");
		XPublisher bids = new XPublisher("tcp://*:5571","bids");
		XPublisher clicks = new XPublisher("tcp://*:5573","clicks");

		for (int j = 0; j < 1000; j++) {
			wins.add(obj);
			bids.add(br);
			cmd.type = PixelClickConvertLog.CLICK;
			clicks.add(cmd);
			cmd.type = PixelClickConvertLog.PIXEL;
			clicks.add(cmd);
		}
	}

}
