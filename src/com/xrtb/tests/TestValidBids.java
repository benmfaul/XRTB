package com.xrtb.tests;

import static org.junit.Assert.*;

import java.io.InputStream;
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
import org.redisson.Redisson;
import org.redisson.RedissonClient;
import org.redisson.core.MessageListener;
import org.redisson.core.RTopic;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xrtb.bidder.Controller;
import com.xrtb.bidder.RTBServer;
import com.xrtb.commands.BasicCommand;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.HttpPostGet;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;

import junit.framework.TestCase;

/**
 * A class for testing that the bid has the right parameters
 * @author Ben M. Faul
 *
 */
public class TestValidBids  {
	static Controller c;
	public static String test = "";
	static Gson gson = new Gson();
	
	static RedissonClient redisson;
	static RTopic bids;
	static BidResponse response;
	static CountDownLatch latch;
	@BeforeClass
	  public static void testSetup() {		
		try {
			Config.setup();
			
			org.redisson.Config cfg = new org.redisson.Config();
			cfg.useSingleServer()
	    	.setAddress("localhost:6379")
	    	.setConnectionPoolSize(10);
	
			
			redisson = Redisson.create(cfg);
			bids = redisson.getTopic("bids");
			bids.addListener(new MessageListener<BidResponse>() {
				@Override
				public void onMessage(String channel, BidResponse br) {
					//System.out.println("<<<<<<<<<<<<<<<<<" + br);
					response = br;
					if (latch == null)
						return;
					latch.countDown();
				}
			}); 

		} catch (Exception error) {
			error.printStackTrace();
		}
	  }

	  @AfterClass
	  public static void testCleanup() {
		Config.teardown();
	  }
	  
	  @Test
	  public void testSetBidFloor() throws Exception {
		  BidRequest br = new BidRequest("./SampleBids/nexage.txt");
		  assertNotNull(br);
		  assertNull(br.bidFloor);
		 
		  br.setBidFloor(100.0);  
		  JsonNode n = (JsonNode) br.getNode("imp.0.bidfloor");
		  assertNotNull(n);
		  assertTrue(n.doubleValue() == 100.0);
	  }
	  
	  /**
	   * Test a valid bid response.
	   * @throws Exception on networking errors.
	   */
	  @Test 
	  public void testBannerRespondWithBid() throws Exception {
			HttpPostGet http = new HttpPostGet();
			String s = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/nexage.txt")))).toString();
			long time = 0;
			
			try {
				 http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", s);
			} catch (Exception error) {
				fail("Network error");
			}
			String xtime = null;
			try {
				s = Charset
						.defaultCharset()
						.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
								.get("./SampleBids/nexage.txt")))).toString();
				try {
					time = System.currentTimeMillis();
					s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", s);
					time = System.currentTimeMillis() - time;
					xtime = http.getHeader("X-TIME");
				} catch (Exception error) {
					fail("Can't connect to test host: " + Config.testHost);
				}
				gson = new GsonBuilder().setPrettyPrinting().create();
				Map m = null;
				try {
					m = gson.fromJson(s,Map.class);
				} catch (Exception error) {
					fail("Bad JSON for bid");
				}
				List list =  (List)m.get("seatbid");
				m = (Map)list.get(0);
				assertNotNull(m);
				String test =(String) m.get("seat");
				assertTrue(test.equals("99999999"));
				list =(List)m.get("bid");
				assertEquals(list.size(),1);
				m = (Map)list.get(0);
				assertNotNull(m);
				test = (String)m.get("impid");
				assertTrue(test.equals("1"));
				test = (String)m.get("id");
				assertTrue(test.equals("35c22289-06e2-48e9-a0cd-94aeb79fab43"));
				double d = (Double)m.get("price");
				assertTrue(d==1.0);
				
				test = (String)m.get("adid");
				
				assertTrue(test.equals("ben:payday"));
				
				test = (String)m.get("cid");
				assertTrue(test.equals("ben:payday"));
				
				test = (String)m.get("crid");
				assertTrue(test.contains("-skiddoo"));
				
				list = (List)m.get("adomain");
				test = (String)list.get(0);
				assertTrue(test.equals("originator.com"));
				
				System.out.println("XTIME: " + xtime);
				System.out.println("RTTIME: " + time);
				System.out.println(s);
				
				assertTrue(s.contains("nurl"));
				assertTrue(s.contains("cid"));
				assertTrue(s.contains("iurl"));

			} catch (Exception e) {
				e.printStackTrace();
				fail(e.toString());

			}
		} 
	  
	  @Test 
	  public void testAtomx() throws Exception {
			HttpPostGet http = new HttpPostGet();
			String s = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/atomx.txt")))).toString();
			long time = 0;
			
			String xtime = null;
			try {
				s = Charset
						.defaultCharset()
						.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
								.get("./SampleBids/nexage.txt")))).toString();
				try {
					time = System.currentTimeMillis();
					s = http.sendPost("http://" + Config.testHost + "/rtb/bids/atomx", s,100000,100000);
					time = System.currentTimeMillis() - time;
					xtime = http.getHeader("X-TIME");
				} catch (Exception error) {
					fail("Can't connect to test host: " + Config.testHost);
				}
				assertNotNull(s);
				gson = new GsonBuilder().setPrettyPrinting().create();
				Map m = null;
				try {
					m = gson.fromJson(s,Map.class);
				} catch (Exception error) {
					fail("Bad JSON for bid");
				}
				List list =  (List)m.get("seatbid");
				m = (Map)list.get(0);
				assertNotNull(m);
				String test =(String) m.get("seat");
				assertTrue(test.equals("atomxseatid"));
				list =(List)m.get("bid");
				assertEquals(list.size(),1);
				m = (Map)list.get(0);
				assertNotNull(m);
				test = (String)m.get("impid");
				assertTrue(test.equals("1"));
				test = (String)m.get("id");
				assertTrue(test.equals("35c22289-06e2-48e9-a0cd-94aeb79fab43"));
				double d = (Double)m.get("price");
				assertTrue(d==1.0);
				
				test = (String)m.get("adid");
				
				assertTrue(test.equals("ben:payday"));
				
				test = (String)m.get("cid");
				assertTrue(test.equals("ben:payday"));
				
				test = (String)m.get("crid");
				assertTrue(test.contains("-skiddoo"));
				
				list = (List)m.get("adomain");
				test = (String)list.get(0);
				assertTrue(test.equals("originator.com"));
				
				System.out.println("XTIME: " + xtime);
				System.out.println("RTTIME: " + time);
				System.out.println(s);
				
				assertFalse(s.contains("pub"));
				assertFalse(s.contains("ad_id"));
				assertFalse(s.contains("bid_id"));
				assertFalse(s.contains("site_id"));

			} catch (Exception e) {
				e.printStackTrace();
				fail(e.toString());

			}
		} 
	  
	  /**
	   * Test a valid bid response.
	   * @throws Exception on networking errors.
	   */
	  @Test 
	  public void testVideoRespondWithBid() throws Exception {
			HttpPostGet http = new HttpPostGet();
			String s = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/nexageVideo.txt")))).toString();
			long time = 0;
			
			/******** Make one bid to prime the pump */
			try {
				 http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", s);
			} catch (Exception error) {
				fail("Network error");
			}
			/*********************************/
			String xtime = null;
			try {
				s = Charset
						.defaultCharset()
						.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
								.get("./SampleBids/nexageVideo.txt")))).toString();
				try {
					time = System.currentTimeMillis();
					s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", s);
					time = System.currentTimeMillis() - time;
					xtime = http.getHeader("X-TIME");
				} catch (Exception error) {
					fail("Can't connect to test host: " + Config.testHost);
				}
				assertNotNull(s);
				System.out.println(s+"\n----------");
				gson = new GsonBuilder().setPrettyPrinting().create();
				Map m = null;
				try {
					m = gson.fromJson(s,Map.class);
				} catch (Exception error) {
					fail("Bad JSON for bid");
				}
				List list =  (List)m.get("seatbid");
				m = (Map)list.get(0);
				assertNotNull(m);
				String test =(String) m.get("seat");
				assertTrue(test.equals("99999999"));
				list =(List)m.get("bid");
				assertEquals(list.size(),1);
				m = (Map)list.get(0);
				assertNotNull(m);
				test = (String)m.get("impid");
				assertTrue(test.equals("35c22289-06e2-48e9-a0cd-94aeb79fab43-1"));
				test = (String)m.get("id");
				assertTrue(test.equals("35c22289-06e2-48e9-a0cd-94aeb79fab43"));
				double d = (Double)m.get("price");
				assertTrue(d==3.0);
				
				test = (String)m.get("adid");
				
				assertTrue(test.equals("ben:payday"));
				
				test = (String)m.get("cid");
				assertTrue(test.equals("ben:payday"));
				
				test = (String)m.get("crid");
				assertTrue(test.equals("iAmVideo"));
				
				list = (List)m.get("adomain");
				test = (String)list.get(0);
				assertTrue(test.equals("originator.com"));
				
				System.out.println("XTIME: " + xtime);
				System.out.println("RTTIME: " + time);
				System.out.println(s);
				
				assertFalse(s.contains("pub"));
				assertFalse(s.contains("ad_id"));
				assertFalse(s.contains("bid_id"));
				assertFalse(s.contains("site_id"));

			} catch (Exception e) {
				e.printStackTrace();
				fail(e.toString());

			}
		} 
	  
	  /**
	   * Test a valid bid response.
	   * @throws Exception on networking errors.
	   */
	  @Test 
	  public void testVideoRespondWithBidAfterRecompile() throws Exception {
			HttpPostGet http = new HttpPostGet();
			String s = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/nexageVideo.txt")))).toString();
			long time = 0;
			
			/******** Make one bid to prime the pump */
			try {
				 http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", s);
			} catch (Exception error) {
				fail("Network error");
			}
			/*********************************/
			
			Configuration.getInstance().recompile();
			String xtime = null;
			try {
				s = Charset
						.defaultCharset()
						.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
								.get("./SampleBids/nexageVideo.txt")))).toString();
				try {
					time = System.currentTimeMillis();
					s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", s);
					time = System.currentTimeMillis() - time;
					xtime = http.getHeader("X-TIME");
				} catch (Exception error) {
					fail("Can't connect to test host: " + Config.testHost);
				}
				assertNotNull(s);
				System.out.println(s+"\n----------");
				gson = new GsonBuilder().setPrettyPrinting().create();
				Map m = null;
				try {
					m = gson.fromJson(s,Map.class);
				} catch (Exception error) {
					fail("Bad JSON for bid");
				}
				List list =  (List)m.get("seatbid");
				m = (Map)list.get(0);
				assertNotNull(m);
				String test =(String) m.get("seat");
				assertTrue(test.equals("99999999"));
				list =(List)m.get("bid");
				assertEquals(list.size(),1);
				m = (Map)list.get(0);
				assertNotNull(m);
				test = (String)m.get("impid");
				assertTrue(test.equals("35c22289-06e2-48e9-a0cd-94aeb79fab43-1"));
				test = (String)m.get("id");
				assertTrue(test.equals("35c22289-06e2-48e9-a0cd-94aeb79fab43"));
				double d = (Double)m.get("price");
				assertTrue(d==3.0);
				
				test = (String)m.get("adid");
				
				assertTrue(test.equals("ben:payday"));
				
				test = (String)m.get("cid");
				assertTrue(test.equals("ben:payday"));
				
				test = (String)m.get("crid");
				assertTrue(test.equals("iAmVideo"));
				
				list = (List)m.get("adomain");
				test = (String)list.get(0);
				assertTrue(test.equals("originator.com"));
				
				System.out.println("XTIME: " + xtime);
				System.out.println("RTTIME: " + time);
				System.out.println(s);
				
				assertFalse(s.contains("pub"));
				assertFalse(s.contains("ad_id"));
				assertFalse(s.contains("bid_id"));
				assertFalse(s.contains("site_id"));

			} catch (Exception e) {
				e.printStackTrace();
				fail(e.toString());

			}
		} 
	  
	  
	  /**
	   * Test a valid bid response with no bid, the campaign doesn't match width or height of the bid request
	   * @throws Exception on network errors.
	   */
	  @Test 
	  public void testRespondWithNoBid() throws Exception {
			HttpPostGet http = new HttpPostGet();
			String s = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/nexage50x50.txt")))).toString();
			try {
				 s = s.replaceAll("50,", "49,"); // make it an odd size so there is no match
				 s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", s);
			} catch (Exception error) {
				fail("Network error");
			}
			assertTrue(http.getResponseCode()==204);
			assertTrue(http.getHeader("X-REASON").equals("No matching campaign"));
		} 
	  
	  
	  /**
	   * Test Native App Wall content advertising
	   * @throws Exception on network and configuration errors/
	   */
	  @Test 
	  public void testNativeContentStreaming() throws Exception {
			HttpPostGet http = new HttpPostGet();
			String bid = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/nexageNativeContentStreamWithVideo.txt")))).toString();
		    String s = null;
			long time = 0;
			
			/******** Make one bid to prime the pump */
			try {
				 http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", bid,300000,300000);
			} catch (Exception error) {
				fail("Network error");
			}
			/*********************************/
			String xtime = null;
			try {
				try {
					time = System.currentTimeMillis();
					s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", bid,300000,300000);
					time = System.currentTimeMillis() - time;
					xtime = http.getHeader("X-TIME");
				} catch (Exception error) {
					fail("Can't connect to test host: " + Config.testHost);
				}
				assertNotNull(s);
				System.out.println(s+"\n----------");
				gson = new GsonBuilder().setPrettyPrinting().create();
				Map m = null;
				try {
					m = gson.fromJson(s,Map.class);
				} catch (Exception error) {
					fail("Bad JSON for bid");
				}
				List list =  (List)m.get("seatbid");
				m = (Map)list.get(0);
				assertNotNull(m);
				String test =(String) m.get("seat");
				assertTrue(test.equals("99999999"));
				list =(List)m.get("bid");
				assertEquals(list.size(),1);
				m = (Map)list.get(0);
				assertNotNull(m);
				test = (String)m.get("impid");
				assertTrue(test.equals("39c22289-06e2-48e9-a0cd-94aeb79fab43-1=3"));
				double d = (Double)m.get("price");
				assertTrue(d==10.5);
				
				test = (String)m.get("adid");
				
				assertTrue(test.equals("ben:payday"));
				
				test = (String)m.get("cid");
				assertTrue(test.equals("ben:payday"));
				
				test = (String)m.get("crid");
				assertTrue(test.equals("iAmStreamingContentVideo"));
				
				list = (List)m.get("adomain");
				test = (String)list.get(0);
				assertTrue(test.equals("originator.com"));
				
				System.out.println("XTIME: " + xtime);
				System.out.println("RTTIME: " + time);
				System.out.println(s);
				
				assertFalse(s.contains("pub"));
				assertFalse(s.contains("ad_id"));
				assertFalse(s.contains("bid_id"));
				assertFalse(s.contains("site_id"));
				
				/**
				 * Remove the layout in the request, it should still bid
				 */
				Map map = (Map)gson.fromJson(bid,Map.class);
				list = (List)map.get("imp");
				Map sub = (Map)list.get(0);
				sub = (Map)sub.get("native");
				sub.remove("layout");
				s = gson.toJson(map);
				try {
					time = System.currentTimeMillis();
					s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", s, 3000000, 3000000);
					time = System.currentTimeMillis() - time;
					xtime = http.getHeader("X-TIME");
				} catch (Exception error) {
					fail("Can't connect to test host: " + Config.testHost);
				}
				assertNotNull(s);				
				
				

			} catch (Exception e) {
				e.printStackTrace();
				fail(e.toString());

			}
			
		} 
	  
	  @Test 
	  public void testNativeAppWall() throws Exception {
			HttpPostGet http = new HttpPostGet();
			String bid = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/nexageNativeAppWall.txt")))).toString();
		    String s = null;
			long time = 0;
			
			/******** Make one bid to prime the pump */
			try {
				 http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", bid);
			} catch (Exception error) {
				fail("Network error");
			}
			/*********************************/
			String xtime = null;
			try {
				try {
					time = System.currentTimeMillis();
					s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", bid);
					time = System.currentTimeMillis() - time;
					xtime = http.getHeader("X-TIME");
				} catch (Exception error) {
					fail("Can't connect to test host: " + Config.testHost);
				}
				assertNotNull(s);
				System.out.println(s+"\n----------");
				gson = new GsonBuilder().setPrettyPrinting().create();
				Map m = null;
				try {
					m = gson.fromJson(s,Map.class);
				} catch (Exception error) {
					fail("Bad JSON for bid");
				}
				List list =  (List)m.get("seatbid");
				m = (Map)list.get(0);
				assertNotNull(m);
				String test =(String) m.get("seat");
				assertTrue(test.equals("99999999"));
				list =(List)m.get("bid");
				assertEquals(list.size(),1);
				m = (Map)list.get(0);
				assertNotNull(m);
				test = (String)m.get("impid");
				assertTrue(test.equals("39c22289-06e2-48e9-a0cd-94aeb79fab43-1=3"));
				test = (String)m.get("id");
				assertTrue(test.equals("35c22289-06e2-48e9-a0cd-94aeb79fab43"));
				double d = (Double)m.get("price");
				assertTrue(d==10.0);
				
				test = (String)m.get("adid");
				
				assertTrue(test.equals("ben:payday"));
				
				test = (String)m.get("cid");
				assertTrue(test.equals("ben:payday"));
				
				test = (String)m.get("crid");
				assertTrue(test.equals("iAmAnAppWall"));
				
				list = (List)m.get("adomain");
				test = (String)list.get(0);
				assertTrue(test.equals("originator.com"));
				
				System.out.println("XTIME: " + xtime);
				System.out.println("RTTIME: " + time);
				System.out.println(s);
				
				assertFalse(s.contains("pub"));
				assertFalse(s.contains("ad_id"));
				assertFalse(s.contains("bid_id"));
				assertFalse(s.contains("site_id"));
				
				/**
				 * Remove the layout in the request, it should still bid
				 */
				Map map = (Map)gson.fromJson(bid,Map.class);
				list = (List)map.get("imp");
				Map sub = (Map)list.get(0);
				sub = (Map)sub.get("native");
				sub.remove("layout");
				s = gson.toJson(map);
				try {
					time = System.currentTimeMillis();
					s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", s);
					time = System.currentTimeMillis() - time;
					xtime = http.getHeader("X-TIME");
				} catch (Exception error) {
					fail("Can't connect to test host: " + Config.testHost);
				}
				assertNotNull(s);				
				
				
				/*
				 * Make the title too short
				 */
				map = (Map)gson.fromJson(bid,Map.class);
				list = (List)map.get("imp");
				sub = (Map)list.get(0);
				sub = (Map)sub.get("native");
				list = (List)sub.get("assets");
				sub = (Map)list.get(0);
				sub = (Map)sub.get("title");
				sub.put("len", 1);
				s = gson.toJson(map);
				System.out.println(s);
				try {
					time = System.currentTimeMillis();
					s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", s);
					time = System.currentTimeMillis() - time;
					xtime = http.getHeader("X-TIME");
				} catch (Exception error) {
					fail("Can't connect to test host: " + Config.testHost);
				}
				assertNull(s);
				
				
				/*
				 * Make img have wrong size
				 */
				map = (Map)gson.fromJson(bid,Map.class);
				list = (List)map.get("imp");
				sub = (Map)list.get(0);
				sub = (Map)sub.get("native");
				list = (List)sub.get("assets");
				sub = (Map)list.get(2);
				sub = (Map)sub.get("img");
				sub.put("w", 1);
				s = gson.toJson(map);
				try {
					time = System.currentTimeMillis();
					s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", s);
					time = System.currentTimeMillis() - time;
					xtime = http.getHeader("X-TIME");
				} catch (Exception error) {
					fail("Can't connect to test host: " + Config.testHost);
				}
				assertNull(s);
				
				/*
				 * Make img have wrong size
				 */
				map = (Map)gson.fromJson(bid,Map.class);
				list = (List)map.get("imp");
				sub = (Map)list.get(0);
				sub = (Map)sub.get("native");
				list = (List)sub.get("assets");
				sub = (Map)list.get(2);
				sub = (Map)sub.get("img");
				sub.put("h", 1);
				s = gson.toJson(map);
				try {
					time = System.currentTimeMillis();
					s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", s);
					time = System.currentTimeMillis() - time;
					xtime = http.getHeader("X-TIME");
				} catch (Exception error) {
					fail("Can't connect to test host: " + Config.testHost);
				}
				assertNull(s);
				
				/*
				 * Make the length to long on a data item
				 */
				map = (Map)gson.fromJson(bid,Map.class);
				list = (List)map.get("imp");
				sub = (Map)list.get(0);
				sub = (Map)sub.get("native");
				list = (List)sub.get("assets");
				sub = (Map)list.get(3);
				sub = (Map)sub.get("data");
				sub.put("len", 1);
				s = gson.toJson(map);
				try {
					time = System.currentTimeMillis();
					s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", s);
					time = System.currentTimeMillis() - time;
					xtime = http.getHeader("X-TIME");
				} catch (Exception error) {
					fail("Can't connect to test host: " + Config.testHost);
				}
				assertNull(s);
				
				/*
				 * Make it the wrong layout
				 */
				map = (Map)gson.fromJson(bid,Map.class);
				list = (List)map.get("imp");
				sub = (Map)list.get(0);
				sub = (Map)sub.get("native");
				sub.put("layout",4);

				s = gson.toJson(map);
				try {
					time = System.currentTimeMillis();
					s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", s);
					time = System.currentTimeMillis() - time;
					xtime = http.getHeader("X-TIME");
				} catch (Exception error) {
					fail("Can't connect to test host: " + Config.testHost);
				}
				assertNull(s);
				

			} catch (Exception e) {
				e.printStackTrace();
				fail(e.toString());

			}
			
		} 
	  
	  @Test 
	  public void testFyberPrivateMkt() throws Exception {
			HttpPostGet http = new HttpPostGet();
			String bid = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/fyberVideoPvtMkt.txt")))).toString();
		    String s = null;
			long time = 0;
			String xtime = null;
			
			//Configuration.getInstance().printNoBidReason = true;
			//Configuration.getInstance().logLevel = -5;
			
			// TODO: Need a video campaign to test with this.
			
			try {
				try {
					time = System.currentTimeMillis();
					s = http.sendPost("http://" + Config.testHost + "/rtb/bids/fyber", bid);
					time = System.currentTimeMillis() - time;
					xtime = http.getHeader("X-TIME");
				} catch (Exception error) {
					fail("Can't connect to test host: " + Config.testHost);
				}
				assertNotNull(s);
				

			} catch (Exception e) {
				e.printStackTrace();
				fail(e.toString());

			}
			
		} 
	  
	  @Test 
	  public void testFyberEncoding() throws Exception {
			HttpPostGet http = new HttpPostGet();
			String bid = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/nexage.txt")))).toString();
		    String s = null;
			long time = 0;
			String xtime = null;
			
			try {
				try {
					time = System.currentTimeMillis();
					s = http.sendPost("http://" + Config.testHost + "/rtb/bids/fyber", bid);
					time = System.currentTimeMillis() - time;
					xtime = http.getHeader("X-TIME");
				} catch (Exception error) {
					fail("Can't connect to test host: " + Config.testHost);
				}
				assertNotNull(s);
				int index = s.indexOf("%3C");
				assertTrue(index==-1);
				

			} catch (Exception e) {
				e.printStackTrace();
				fail(e.toString());

			}
			
		} 
	  
	  @Test 
	  public void testAtomxEncoding() throws Exception {
			HttpPostGet http = new HttpPostGet();
			String bid = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/nexage.txt")))).toString();
		    String s = null;
			long time = 0;
			String xtime = null;
			
			try {
				try {
					time = System.currentTimeMillis();
					s = http.sendPost("http://" + Config.testHost + "/rtb/bids/atomx", bid);
					time = System.currentTimeMillis() - time;
					xtime = http.getHeader("X-TIME");
				} catch (Exception error) {
					fail("Can't connect to test host: " + Config.testHost);
				}
				assertNotNull(s);
				int index = s.indexOf("%3C");
				assertTrue(index==-1);
				

			} catch (Exception e) {
				e.printStackTrace();
				fail(e.toString());

			}
			
		} 
	  

	  
	  @Test 
	  public void testCapptureEncoding() throws Exception {
			HttpPostGet http = new HttpPostGet();
			String bid = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/nexage.txt")))).toString();
		    String s = null;
			long time = 0;
			String xtime = null;
			
			try {
				try {
					time = System.currentTimeMillis();
					s = http.sendPost("http://" + Config.testHost + "/rtb/bids/cappture", bid);
					time = System.currentTimeMillis() - time;
					xtime = http.getHeader("X-TIME");
				} catch (Exception error) {
					fail("Can't connect to test host: " + Config.testHost);
				}
				assertNotNull(s);
				int index = s.indexOf("%3C");
				assertTrue(index==-1);
				

			} catch (Exception e) {
				e.printStackTrace();
				fail(e.toString());

			}
			
		} 
	  
	  @Test 
	  public void testCapptureOverride() throws Exception {
			HttpPostGet http = new HttpPostGet();
			String bid = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/cappture-override.txt")))).toString();
		    String s = null;
			long time = 0;
			String xtime = null;
			
			try {
				try {
					time = System.currentTimeMillis();
					s = http.sendPost("http://" + Config.testHost + "/rtb/bids/cappture", bid);
					time = System.currentTimeMillis() - time;
					xtime = http.getHeader("X-TIME");
				} catch (Exception error) {
					fail("Can't connect to test host: " + Config.testHost);
				}
				assertNotNull(s);
				int index = s.indexOf("%3C");
				assertTrue(index==-1);
				index = s.indexOf("override");
				assertTrue(index>-1);
				System.out.println(s);
				

			} catch (Exception e) {
				e.printStackTrace();
				fail(e.toString());

			}
			
		} 
	  
	  @Test 
	  public void testEpomEncoding() throws Exception {
			HttpPostGet http = new HttpPostGet();
			String bid = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/nexage.txt")))).toString();
		    String s = null;
			long time = 0;
			String xtime = null;
			
			try {
				try {
					time = System.currentTimeMillis();
					s = http.sendPost("http://" + Config.testHost + "/rtb/bids/epomx", bid);
					time = System.currentTimeMillis() - time;
					xtime = http.getHeader("X-TIME");
				} catch (Exception error) {
					fail("Can't connect to test host: " + Config.testHost);
				}
				assertNotNull(s);
				int index = s.indexOf("%3C");
				assertTrue(index==-1);
				

			} catch (Exception e) {
				e.printStackTrace();
				fail(e.toString());

			}
			
		} 
	  
	  @Test 
	  public void testNexageEncoding() throws Exception {
			HttpPostGet http = new HttpPostGet();
			String bid = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/nexage.txt")))).toString();
		    String s = null;
			long time = 0;
			String xtime = null;
			
			try {
				try {
					time = System.currentTimeMillis();
					s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", bid,30000,30000);
					time = System.currentTimeMillis() - time;
					xtime = http.getHeader("X-TIME");
				} catch (Exception error) {
					fail("Can't connect to test host: " + Config.testHost);
				}
				assertNotNull(s);
				int index = s.indexOf("%3C");
				assertTrue(index!=-1);
				

			} catch (Exception e) {
				e.printStackTrace();
				fail(e.toString());

			}
			
		} 
	  
	  @Test 
	  public void testSmartyadsEncoding() throws Exception {
			HttpPostGet http = new HttpPostGet();
			String bid = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/nexage.txt")))).toString();
		    String s = null;
			long time = 0;
			String xtime = null;
			
			try {
				try {
					time = System.currentTimeMillis();
					s = http.sendPost("http://" + Config.testHost + "/rtb/bids/smartyads", bid);
					time = System.currentTimeMillis() - time;
					xtime = http.getHeader("X-TIME");
				} catch (Exception error) {
					fail("Can't connect to test host: " + Config.testHost);
				}
				assertNotNull(s);
				int index = s.indexOf("%3C");
				assertTrue(index==-1);
				

			} catch (Exception e) {
				e.printStackTrace();
				fail(e.toString());

			}
			
		} 
	  
	  /**
	   * Test a valid bid response with no bid, the campaign doesn't match width or height of the bid request
	   * @throws Exception on network errors.
	   */
	  @Test 
	  public void testInterstitial() throws Exception {
			HttpPostGet http = new HttpPostGet();
			String s = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/interstitial.txt")))).toString();
			try {
				 s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", s, 100000, 100000);
			} catch (Exception error) {
				fail("Network error");
			}
			System.out.println(s);
			int rc = http.getResponseCode();
			assertTrue(rc==204);
			assertTrue(http.getHeader("X-REASON").equals("No matching campaign"));
		} 

	  /**
	   * Test a valid bid response with no bid, the campaign doesn't match width or height of the bid request
	   * @throws Exception on network errors.
	   */
	  @Test 
	  public void testJavaScriptAppId() throws Exception {
			response = null;
			latch = new CountDownLatch(1);
			HttpPostGet http = new HttpPostGet();
			String s = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/apptest.txt")))).toString();
			try {
				 s = http.sendPost("http://" + Config.testHost + "/rtb/bids/smaato", s, 1000000, 1000000);
			} catch (Exception error) {
				fail("Network error");
			}
			latch.await();
			System.out.println(s);
			int rc = http.getResponseCode();
			assertTrue(rc==200);
			assertFalse(response.forwardUrl.contains("{app_id}"));
	  }

	  @Test 
	  public void testAdIdSubInBannerAndJavascript() throws Exception {
			HttpPostGet http = new HttpPostGet();
			String bid = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/nexageJavaScript.txt")))).toString();
		    String s = null;
			long time = 0;
			String xtime = null;
			
			try {
				try {
					time = System.currentTimeMillis();
					s = http.sendPost("http://" + Config.testHost + "/rtb/bids/smaato", bid,300000,300000);
					time = System.currentTimeMillis() - time;
					xtime = http.getHeader("X-TIME");
				} catch (Exception error) {
					fail("Can't connect to test host: " + Config.testHost);
				}
				assertNotNull(s);
				assertFalse(s.contains("app_id"));
				assertFalse(s.contains("{bid}"));

			} catch (Exception e) {
				e.printStackTrace();
				fail(e.toString());

			}
			
		} 
	  
	  @Test 
	  public void testSuspect() throws Exception {
			HttpPostGet http = new HttpPostGet();
			String bid = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/suspect.txt")))).toString();
		    String s = null;
			long time = 0;
			String xtime = null;
			
			try {
				try {
					time = System.currentTimeMillis();
					s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", bid,300000,300000);
					time = System.currentTimeMillis() - time;
					xtime = http.getHeader("X-TIME");
				} catch (Exception error) {
					fail("Can't connect to test host: " + Config.testHost);
				}

			} catch (Exception e) {
				e.printStackTrace();
				fail(e.toString());

			}
			
		} 
	  
	  @Test 
	  public void testSuspect2() throws Exception {
			HttpPostGet http = new HttpPostGet();
			String bid = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/another-suspect.txt")))).toString();
		    String s = null;
			long time = 0;
			String xtime = null;
			
			try {
				try {
					time = System.currentTimeMillis();
					s = http.sendPost("http://" + Config.testHost + "/rtb/bids/nexage", bid,300000,300000);
					time = System.currentTimeMillis() - time;
					xtime = http.getHeader("X-TIME");
				} catch (Exception error) {
					fail("Can't connect to test host: " + Config.testHost);
				}

			} catch (Exception e) {
				e.printStackTrace();
				fail(e.toString());

			}
			
		} 
}
