package test.java;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import com.xrtb.bidder.Controller;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.HttpPostGet;
import com.xrtb.jmq.MessageListener;
import com.xrtb.jmq.RTopic;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;
import com.xrtb.tools.DbTools;

/**
 * A class for testing that the bid has the right parameters
 * 
 * @author Ben M. Faul
 *
 */
public class TestLucene {
	static Controller c;
	public static String test = "";

	static BidResponse response;
	static CountDownLatch latch;

	@BeforeClass
	public static void testSetup() {
		try {
			Config.setup();
		} catch (Exception error) {
			error.printStackTrace();
		}
	}

	@AfterClass
	public static void testCleanup() {
		Config.teardown();
	}
	
	@Test
	public void testLucene() {
		Campaign c = Configuration.getInstance().campaignsList.get(0);
		System.out.println(c.getLucene());
	}
}