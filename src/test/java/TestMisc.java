package test.java;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xrtb.exchanges.C1X;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;

import junit.framework.TestCase;

/**
 * Tests miscellaneous classes.
 * @author Ben M. Faul
 *
 */

public class TestMisc {

	@BeforeClass
	public static void setup() {
		try {
			Config.setup();
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
	public void testAssortedMethods() {
		String test = "site_id and then some text and site_id and some more text and finally site_id";
		
		StringBuilder sb = new StringBuilder(test);
		
		BidResponse.replaceAll(sb,null,"XXX");
		assertTrue(sb.toString().equals(test));
		
		BidResponse.replaceAll(sb,"XXX",null);
		assertTrue(sb.toString().equals(test));
		
		BidResponse.replaceAll(sb,"site_id","XXX");
		test = sb.toString();
		assertFalse(test.contains("site_id"));
	}
	
	@Test
	public  void testHttpInC1x() throws Exception {
		String data = Charset.defaultCharset()
				.decode(ByteBuffer.wrap(Files.readAllBytes(Paths.get("./SampleBids/nexage.txt")))).toString();
		data = data.replaceAll("junk1.com", "http://junk1.com");
		C1X x  = new C1X(new StringBuilder(data));
		System.out.println(x.getOriginal());
		assertTrue(x.siteDomain.equals("junk1.com"));
		
		data = Charset.defaultCharset()
				.decode(ByteBuffer.wrap(Files.readAllBytes(Paths.get("./SampleBids/nexage.txt")))).toString();
		data = data.replace("site", "app");
		data = data.replaceAll("junk1.com", "http://junk1.com");
		x  = new C1X(new StringBuilder(data));
		System.out.println(x.getOriginal());
		assertTrue(x.siteDomain.equals("junk1.com"));
		
		data = Charset.defaultCharset()
				.decode(ByteBuffer.wrap(Files.readAllBytes(Paths.get("./SampleBids/nexage.txt")))).toString();
		data = data.replace("site", "app");
		data = data.replaceAll("junk1.com", "https://junk1.com");
		x  = new C1X(new StringBuilder(data));
		System.out.println(x.getOriginal());
		assertTrue(x.siteDomain.equals("junk1.com"));
		
		data = Charset.defaultCharset()
				.decode(ByteBuffer.wrap(Files.readAllBytes(Paths.get("./SampleBids/nexage.txt")))).toString();
		data = data.replaceAll("junk1.com", "https://junk1.com");
		x  = new C1X(new StringBuilder(data));
		System.out.println(x.getOriginal());
		assertTrue(x.siteDomain.equals("junk1.com"));
	}
}
