package test.java;

import static org.junit.Assert.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.xrtb.bidder.Controller;
import com.xrtb.fraud.ForensiqClient;
import com.xrtb.fraud.FraudLog;
import com.xrtb.fraud.MMDBClient;
/**
 * A class for testing that the bid has the right parameters
 * @author Ben M. Faul
 *
 */
public class TestFraud  {
	static Controller c;
	public static String test = "";
	
	@BeforeClass
	  public static void testSetup() {		
		try {
			Config.setup();
			Config.setup();System.out.println("******************  TestForensiq");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }

	  @AfterClass
	  public static void testCleanup() {
		  Config.teardown();
	  }
	  
	  @Test
	  public void testStub() {
		  
	  }
	  
	  /**
	   * Test a valid bid response.
	   * @throws Exception on networking errors.
	   */
	 // @Test 
	  public void testBad() throws Exception {
			ForensiqClient forensiq = ForensiqClient.build("6RLzOXoxntkqr0PHJ1Z0");
			
			String rt = "display";                        												
			String ip = "52.35.123.110";					  												// device.ip
			String url = "http%3A%2F%2Fwww.myheretrtrtouse.com%2Fsections%2Fliving%3Fa%3D3%20";			// site.page
			String ua = "erererer%2F4.0%20(compatible%3B%20MSIE%207.0%3B%20Windoreererws%20NT%206.0";	// device.ua
			String seller = "seller1234";																// site.name		
			String crid = "creative1234";																// your creative id
			
			FraudLog m = forensiq.bid(rt, ip, url, ua, seller, crid);
			assertNotNull(m);
	  }
	  
	  /**
	   * Test a valid bid response.
	   * @throws Exception on networking errors.
	   */
	  //@Test 
	  public void testGood() throws Exception {
		  
		  ForensiqClient forensiq = ForensiqClient.build("YOUR KEY HERE");
			
			String rt = "display";
			String ip = "123.250.33.4";
			String url = "http%3A%2F%2Fwww.myheretrtrtouse.com%2Fsections%2Fliving%3Fa%3D3%20";
			String ua = "erererer%2F4.0%20(compatible%3B%20MSIE%207.0%3B%20Windoreererws%20NT%206.0)";
			String seller = "seller1234";
			String crid = "xyz1234";
			
			FraudLog m = forensiq.bid(rt, ip, url, ua, seller, crid);
			assertNull(m);
		
	  }
	  
	  //@Test 
	  public void testARealBid() throws Exception {
		  	ForensiqClient forensiq =  ForensiqClient.build("YOUR_KEY_HERE");
		  	
		  	String s = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get("./SampleBids/nexage.txt")))).toString();
		  	
		  	
		  	
			String rt = "display";
			String ip = "123.254.33.4";
			String url = "http%3A%2F%2Fwww.myheretrtrtouse.com%2Fsections%2Fliving%3Fa%3D3%20";
			String ua = "erererer%2F4.0%20(compatible%3B%20MSIE%207.0%3B%20Windoreererws%20NT%206.0)";
			String seller = "seller1234";
			String crid = "xyz1234";
			
			FraudLog m = forensiq.bid(rt, ip, url, ua, seller, crid);
			assertNull(m);
		
	  }
	  
	//  @Test
	  public void testMMDBGood() throws Exception {
		  MMDBClient forensiq =  MMDBClient.build("local/GeoIP2-ISP.mmdb");
		  String rt = "display";
			String ip = "123.254.33.4";
			String url = "http%3A%2F%2Fwww.myheretrtrtouse.com%2Fsections%2Fliving%3Fa%3D3%20";
			String ua = "erererer%2F4.0%20(compatible%3B%20MSIE%207.0%3B%20Windoreererws%20NT%206.0)";
			String seller = "seller1234";
			String crid = "xyz1234";
			
			FraudLog m = forensiq.bid(rt, ip, url, ua, seller, crid);
			assertNull(m);
			
			List<String> test = new ArrayList();
			test.add("st");
			forensiq.setWatchlist(test);
			m = forensiq.bid(rt, ip, url, ua, seller, crid);
			m.exchange = "smaato";
			m.id = "32092930293020020";
			System.out.println(m);
			assertNotNull(m);
		  
	  }
}