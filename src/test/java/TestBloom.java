package test.java;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.hash.BloomFilter;
import com.xrtb.bidder.Controller;
import com.xrtb.blocks.Bloom;
import com.xrtb.blocks.LookingGlass;
import com.xrtb.blocks.NavMap;

/**
 * A class for testing that the bid has the right parameters
 * @author Ben M. Faul
 *
 */
public class TestBloom  {
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
	  public void testBloom() throws Exception {
		  
		  
		    new Bloom("$test","data/c1x_cookies.csv");
		    BloomFilter b = (BloomFilter)LookingGlass.get("$test");
		    assertNotNull(b);
		    
		    boolean p = b.mightContain("842AAB10FBA04247B3A9CE00C9172350");
		    
		    BufferedReader br = new BufferedReader(new FileReader("data/c1x_cookies.csv"));
		    String line = null;
		    int nP = 0;
		    int k = 0;
		    while((line = br.readLine()) != null) {
		    	p = b.mightContain(line);
		    	if (p)
		    		nP++;
		    	k++;
		    }
		    assertTrue(k == nP);
	  }
}