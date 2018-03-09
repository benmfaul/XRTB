package test.java;

import com.xrtb.bidder.RTBServer;
import com.xrtb.commands.PixelClickConvertLog;
import com.xrtb.common.HttpPostGet;
import com.xrtb.exchanges.google.GoogleBidRequest;
import com.xrtb.exchanges.google.GoogleBidResponse;
import com.xrtb.tools.DbTools;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.openrtb.json.OpenRtbJsonFactory;
import com.google.openrtb.json.OpenRtbJsonReader;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * A class for testing that the bid has the right parameters
 * 
 * @author Ben M. Faul
 *
 */
public class TestGoogle {
	public static final String testHost = "localhost:8080";
	static final String redisHost = "localhost3000";
	static DbTools tools;
	/** The RTBServer object used in the tests. */
	static RTBServer server;

	@BeforeClass
	public static void testSetup() {
		try {
			Config.setup();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	@AfterClass
	public static void testCleanup() {
		Config.teardown();
	}

	/**
	 * Test a valid bid response.
	 * 
	 * @throws Exception
	 *             on networking errors.
	 */
	@Test
	public void testGoogleProtobufBanner() throws Exception {
		HttpPostGet http = new HttpPostGet();
		GoogleBidRequest google = GoogleBidRequest.fromRTBFile("./SampleBids/nexage.txt");
		byte[] protobytes = google.getInternal().toByteArray();
		byte[] returns = http.sendPost("http://" + Config.testHost + "/rtb/bids/google", protobytes, 300000, 300000);
		int code = http.getResponseCode();
		assertTrue(code == 200);
		assertNotNull(returns);
		GoogleBidResponse rr = new GoogleBidResponse(returns);
		System.out.println(rr.getInternal());

	}

	@Test
	public void testGoogleProtobufVideo() throws Exception {
		RTBServer.frequencyGoverner.clear();
		HttpPostGet http = new HttpPostGet();
		GoogleBidRequest google = GoogleBidRequest.fromRTBFile("./SampleBids/nexageVideo.txt");
		byte[] protobytes = google.getInternal().toByteArray();
		byte[] returns = http.sendPost("http://" + Config.testHost + "/rtb/bids/google", protobytes, 300000, 300000);
		int code = http.getResponseCode();
		assertTrue(code == 200);
		assertNotNull(returns);
		GoogleBidResponse rr = new GoogleBidResponse(returns);
		String str = rr.toString();
		int i = str.indexOf("http");
		str = str.substring(i);
		i = str.indexOf("\"");
		str = str.substring(0,i);
		System.out.println(str);

		String data = http.sendGet(str);
		System.out.println(data);
	}
	
	@Test
	public void testGoogleDecrypt() throws Exception {
		String payload = "http://localhost:8080/rtb/win/chive.com/APP/google/WP5SPgAE9TEKDFtHAAnnOm9LuUuqG14LOdRXXQ/0.0/0.0/55/87/WPfq6wABDYsKUaXKwgwIUw";
		HttpPostGet http = new HttpPostGet();
		http.sendGet(payload,300000,300000);
	}

	@Test
	public void testHasPageUrlButNoSiteDomain() throws Exception {
		RTBServer.frequencyGoverner.clear();
		HttpPostGet http = new HttpPostGet();
		GoogleBidRequest google = GoogleBidRequest.fromRTBFile("./SampleBids/nexageNoDomain.txt");
		byte[] protobytes = google.getInternal().toByteArray();
		byte[] returns = http.sendPost("http://" + Config.testHost + "/rtb/bids/google", protobytes, 300000, 300000);
		int code = http.getResponseCode();
		assertTrue(code == 200);
		assertNotNull(returns);
		GoogleBidResponse rr = new GoogleBidResponse(returns);
		System.out.println(rr.getInternal());
		assertNotNull(google.siteDomain);
		assertFalse(google.siteDomain.contains("www."));
	}

	@Test
	public void testVerticals() throws Exception {
		String proto = Charset.defaultCharset()
				.decode(ByteBuffer
						.wrap(Files.readAllBytes(Paths.get("./SampleBids/nositedomain.proto"))))
				.toString();
		byte[] data = DatatypeConverter.parseBase64Binary(proto);
		InputStream is = new ByteArrayInputStream(data);
		GoogleBidRequest r = new GoogleBidRequest(is);
		Object s = r.interrogate("site.domain");
		String test = s.toString();
		assertTrue(test.contains("mobile.sabq.org"));
		ArrayNode node = (ArrayNode)r.interrogate("site.cat");
		test = node.toString();
		assertTrue(test.contains("5098"));
		assertTrue(test.contains("702"));
		assertTrue(test.contains("666"));
		assertTrue(test.contains("16"));
	}

	@Test
	public void testSlashInGoogleId() throws Exception {
		GoogleBidRequest google = GoogleBidRequest.fromRTBFile("./SampleBids/hasslash.txt");
		String id = google.getId();
		System.out.println(id);
		assertFalse(id.contains("%2F"));

		HttpPostGet http = new HttpPostGet();
		String bidid = "123456/78910/12";
		bidid = URLEncoder.encode(bidid, "UTF-8");
		String pixel = "http://localhost:8080/pixel/exchange=google/ad_id=thead/creative_id=thecreative/price=AAABX/bid_id=" + bidid;
		http.sendPost(pixel, "", 300000, 300000);

		String win = "http://localhost:8080/pixel/exchange=google/ad_id=thead/creative_id=thecreative/price=AAABX/bid_id=" + bidid;
		http.sendPost(win, "", 300000, 300000);
	}

	@Test
	public void testGooglePixelWithSlash() throws Exception {
		HttpPostGet http = new HttpPostGet();
		String bidid = "123456/78910/12";
		bidid = URLEncoder.encode(bidid, "UTF-8");
		String pixel = "http://localhost:8080/pixel/exchange=google/ad_id=thead/creative_id=thecreative/price=AAABX/bid_id=" + bidid;
		http.sendPost(pixel, "", 300000, 300000);
	}

	@Test
	public void testGooglePixelWithPrice() throws Exception {
		String pixel = "/pixel/exchange=google/ad_id=1419/creative_id=2258/price=WeaZ_gAJnSoK0xGqAAKHTBlbhHqTkOzPFSNZJA/bid_id=WeaZ%2FwAKLUQKUYvLxw53mA/site_domain=muslima.com";
		PixelClickConvertLog log = new PixelClickConvertLog();
		log.create(pixel);
		assertTrue(log.price > 0.0);
	}
}

class MyReader extends OpenRtbJsonReader {

	protected MyReader(OpenRtbJsonFactory factory) {
		super(factory);
		// TODO Auto-generated constructor stub
	}

}
