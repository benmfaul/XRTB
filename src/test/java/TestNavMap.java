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
import com.xrtb.common.ForensiqLog;
import com.xrtb.pojo.ForensiqClient;
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
		    NavMap sr = new NavMap("CIDR", "/media/twoterra/CIDR20170119.txt", false);
		    
		    long x = NavMap.ipToLong("223.239.250.0");
		    boolean p = NavMap.searchTable("CIDR", x);
		    assertTrue(p);
		    
		    x = NavMap.ipToLong("223.239.249.0");
		    p = NavMap.searchTable("CIDR", x);
		    assertFalse(p);
		    
		    x = NavMap.ipToLong("223.255.231.255");
		    p = NavMap.searchTable("CIDR", x);
		    assertTrue(p);
		    
		    x = NavMap.ipToLong("223.240.1.0");
		    p = NavMap.searchTable("CIDR", x);
		    assertFalse(p);
		    
		    x = NavMap.ipToLong("192.2.2.0");
		    p = NavMap.searchTable("CIDR", x);
		    assertFalse(p);
	  }
}