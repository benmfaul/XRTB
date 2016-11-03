package test.java;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xrtb.exchanges.adx.AdxWinObject;
import com.xrtb.pojo.BidResponse;

import junit.framework.TestCase;

/**
 * Tests miscellaneous classes.
 * @author Ben M. Faul
 *
 */

public class TestAdx {

	@BeforeClass
	public static void setup() {
		try {
			Config.setup("Campaigns/adx.json");
			System.out.println("******************  TestMisc");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void stop() {
		Config.teardown();
	}
	
	/**
	 * Test the string replace functions used for macro substitutions.
	 */
	@Test
	public void testDecryptionOfPrice() {
		
		assertNotNull(AdxWinObject.encryptionKeyBytes);
		assertNotNull(AdxWinObject.integrityKeyBytes);
		
		String price = "SjpvRwAB4kB7jEpgW5IA8p73ew9ic6VZpFsPnA";
		try {
			price = AdxWinObject.decrypt(price,System.currentTimeMillis());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.toString());
		}
		assertTrue(price.equals("709959680"));
		

	}
	
	@Test
	public void testDecryptionOfHyperLocal() {
		
		assertNotNull(AdxWinObject.encryptionKeyBytes);
		assertNotNull(AdxWinObject.integrityKeyBytes);
		
		String price = "SjpvRwAB4kB7jEpgW5IA8p73ew9ic6VZpFsPnA";
		try {
			price = AdxWinObject.decrypt(price,System.currentTimeMillis());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.toString());
		}
		assertTrue(price.equals("709959680"));
		

	}
}
