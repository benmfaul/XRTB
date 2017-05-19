package test.java;

import static org.junit.Assert.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.xrtb.bidder.Controller;
import com.xrtb.blocks.NavMap;
import com.xrtb.fraud.ForensiqClient;
import com.xrtb.fraud.FraudLog;
/**
 * A class for testing that the bid has the right parameters
 * @author Ben M. Faul
 *
 */
public class TestNavMap  {
	static Controller c;
	public static String test = "";
	
	@BeforeClass
	  public static void testSetup() {		
		
	  }

	  @AfterClass
	  public static void testCleanup() {
		
	  }
	  
	  /**
	   * Test a valid bid response.
	   * @throws Exception on networking errors.
	   */
	  @Test 
	  public void testNavMap() throws Exception {
		    NavMap sr = new NavMap("CIDR", "data/TESTCIDR1.txt", false);
		    
		    long x = NavMap.ipToLong("192.168.0.12");
		    boolean p = NavMap.searchTable("CIDR", x);
		    assertTrue(p);
		    
		    x = NavMap.ipToLong("191.168.0.12");
		    p = NavMap.searchTable("CIDR", x);
		    assertFalse(p);
		    
		    x = NavMap.ipToLong("223.255.231.255");
		    p = NavMap.searchTable("CIDR", x);
		    assertFalse(p);
	  }
}