package test.java;

import static org.junit.Assert.*;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


import com.xrtb.bidder.Controller;

import com.xrtb.exchanges.Fyber;
import com.xrtb.pojo.BidRequest;


/**
 * A class for testing that the bid has the right parameters
 * @author Ben M. Faul
 *
 */
public class TestFyber  {
	static Controller c;
	public static String test = "";
	
	@BeforeClass
	  public static void testSetup() {		
		try {
			Config.setup();
			Config.setup();System.out.println("******************  TestFyber");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }

	  @AfterClass
	  public static void testCleanup() {
		  Config.teardown();
	  }
	  
	  /**
	   * Test a valid bid response.
	   * @throws Exception on networking errors.
	   */
	  @Test 
	  public void testFyberDisplay() throws Exception {
			BidRequest br = new Fyber("./SampleBids/fyberDisplay640x480.txt");
	  }
	  
	  /**
	   * Test a valid bid response.
	   * @throws Exception on networking errors.
	   */
	  @Test 
	  public void testFyberVideoPvt() throws Exception {
			BidRequest br = new Fyber("./SampleBids/fyberVideoPvtMkt.txt");
	  }
	  
	  @Test 
	  public void testMobileInApp() throws Exception {
			BidRequest br = new Fyber("./SampleBids/fyberMobileInApp.txt");
	  }
}