package test.java;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;


import com.xrtb.bidder.ZPublisher;

import com.xrtb.commands.PixelClickConvertLog;

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

	static Spark sp;
	
	@BeforeClass
	public static void setup() {
		System.out.println("******************  TestSpark");
	}
	
	@Test
	public void testNoAuth() {
		try {
			sp = new Spark("localhost");
			transmit();
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

	public void transmit() throws Exception {
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


		ZPublisher  wins = new ZPublisher("tcp://*:5572","wins");
		ZPublisher bids = new ZPublisher("tcp://*:5571","bids");
		ZPublisher clicks = new ZPublisher("tcp://*:5573","clicks");

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
