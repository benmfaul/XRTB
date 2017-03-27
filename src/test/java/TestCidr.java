package test.java;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.jboss.netty.handler.ipfilter.CIDR;
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
public class TestCidr  {
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
	  public void testCidr() throws Exception {
		  
		  
		    NavMap sr = new NavMap("CIDR", "data/METHBOT.txt", true);
		    
		    long x = NavMap.ipToLong("45.33.224.0");
		    boolean p = NavMap.searchTable("CIDR", x);
		    assertTrue(p);
		    
		    x = NavMap.ipToLong("45.33.239.255");
		    p = NavMap.searchTable("CIDR", x);
		    assertTrue(p);
		    
		    x = NavMap.ipToLong("44.33.224.0");
		    p = NavMap.searchTable("CIDR", x); 
		    assertFalse(p);
		    
		    x = NavMap.ipToLong("165.52.0.0");
		    p = NavMap.searchTable("CIDR", x);
		    assertTrue(p);
		    
		    x = NavMap.ipToLong("165.55.255.255");
		    p = NavMap.searchTable("CIDR", x);
		    assertTrue(p);
		    
		    x = NavMap.ipToLong("166.55.255.255");
		    p = NavMap.searchTable("CIDR", x);
		    assertFalse(p);

	  }
}