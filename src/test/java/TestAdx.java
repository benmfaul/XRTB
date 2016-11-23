package test.java;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xrtb.common.HttpPostGet;
import com.xrtb.exchanges.adx.AdxBidRequest;
import com.xrtb.exchanges.adx.AdxWinObject;
import com.xrtb.pojo.BidResponse;

import junit.framework.TestCase;

/**
 * Tests miscellaneous classes.
 * 
 * @author Ben M. Faul
 *
 */

public class TestAdx {

	@BeforeClass
	public static void setup() {
		try {
			Config.setup("local/1trn.json");
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

	@Test
	public void testHttpsGet() throws Exception {
		HttpPostGet hp = new HttpPostGet();
		String data = hp.sendGet("https://rtb4free.com:8081/index.html");
		System.out.println(data);
		assertTrue(data.contains("<html"));

		data = hp.sendPost("https://rtb4free.com:8081/index.html", "");
		assertTrue(data.contains("<html"));
	}

	/**
	 * Test the string replace functions used for macro substitutions.
	 */
	// @Test
	public void testDecryptionOfPrice() {

		assertNotNull(AdxWinObject.encryptionKeyBytes);
		assertNotNull(AdxWinObject.integrityKeyBytes);

		String price = "SjpvRwAB4kB7jEpgW5IA8p73ew9ic6VZpFsPnA";
		try {
			price = AdxWinObject.decrypt(price, System.currentTimeMillis());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.toString());
		}
		assertTrue(price.equals("709959680"));

	}

	@Test
	public void test1Trn() throws Exception {
		String price = "WDR75wAEYL4KabLWAAa_IddPXUo2lHerfFMnXg";
		try {
			price = AdxWinObject.decrypt(price, System.currentTimeMillis());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			fail(e.toString());
		}
		assertTrue(price.equals("100"));

		price = "WDR75wAEYMgKabLWAAa_IVBlOY5n67-nYt9Blw";
		try {
			price = AdxWinObject.decrypt(price, System.currentTimeMillis());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			fail(e.toString());
		}
		assertTrue(price.equals("1100"));

		price = "WDR75wAEYMsKabLWAAa_IWtgx1ZLDWRa4fHyiA";
		try {
			price = AdxWinObject.decrypt(price, System.currentTimeMillis());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			fail(e.toString());
		}
		assertTrue(price.equals("2100"));

		price = "WDR75wAEYM4KabLWAAa_IQEqXbZRMk4Whj5ePg";
		try {
			price = AdxWinObject.decrypt(price, System.currentTimeMillis());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			fail(e.toString());
		}
		assertTrue(price.equals("3100"));

		price = "WDR75wAEYNAKabLWAAa_IT2cIO9XT6vivmyjag";
		try {
			price = AdxWinObject.decrypt(price, System.currentTimeMillis());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			fail(e.toString());
		}
		assertTrue(price.equals("4100"));

		price = "WDR75wAEYNMKabLWAAa_IcVPVwpl2k9QYWEIiA";
		try {
			price = AdxWinObject.decrypt(price, System.currentTimeMillis());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			fail(e.toString());
		}
		assertTrue(price.equals("5100"));

		price = "WDR75wAEYNYKabLWAAa_IZeg4RA2a9T_kbxBFg";
		try {
			price = AdxWinObject.decrypt(price, System.currentTimeMillis());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			fail(e.toString());
		}
		assertTrue(price.equals("6100"));

		price = "WDR75wAEYNgKabLWAAa_ITq-BRcVctHbkmw5OQ";
		try {
			price = AdxWinObject.decrypt(price, System.currentTimeMillis());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			fail(e.toString());
		}
		assertTrue(price.equals("7100"));

		price = "WDR75wAEYNsKabLWAAa_IS0vqeK5PnHtebXy5Q";
		try {
			price = AdxWinObject.decrypt(price, System.currentTimeMillis());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			fail(e.toString());
		}
		assertTrue(price.equals("8100"));

		price = "WDR75wAEYN0KabLWAAa_If90AhHcF8PPKK-_Fg";
		try {
			price = AdxWinObject.decrypt(price, System.currentTimeMillis());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			fail(e.toString());
		}
		assertTrue(price.equals("9100"));

		price = "WDR75wAEYOAKabLWAAa_IeCfgqy5n1I1hJjQiw";
		try {
			price = AdxWinObject.decrypt(price, System.currentTimeMillis());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			fail(e.toString());
		}
		assertTrue(price.equals("10100"));

	}

	@Test
	public void testDecryptionOfHyperLocal() {

	}
}
