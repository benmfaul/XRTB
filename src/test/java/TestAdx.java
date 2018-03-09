package test.java;

import com.xrtb.common.HttpPostGet;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Map;

import static org.junit.Assert.fail;

/**
 * A class for testing that the bid has the right parameters
 * 
 * @author Ben M. Faul
 *
 */
public class TestAdx {

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
	public void testAdx() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader("SampleBids/adxrequests"));
		String data;
		ObjectMapper mapper = new ObjectMapper();
		HttpPostGet http = new HttpPostGet();
		while ((data = br.readLine()) != null) {
			Map map = mapper.readValue(data, Map.class);
			String protobuf = (String) map.get("protobuf");
			if (protobuf != null) {
				byte[] protobytes = DatatypeConverter.parseBase64Binary(protobuf);
				InputStream is = new ByteArrayInputStream(protobytes);
				byte [] returns = http.sendPost("http://" + Config.testHost + "/rtb/bids/adx", protobytes);
				// AdxBidResponse resp = new AdxBidResponse(returns);
				// System.out.println(resp.toString());
			/*	try {
					AdxBidRequest bidRequest = new AdxBidRequest(is);
					System.out.println(bidRequest.internal);
					System.out.println("============================================");
					System.out.println(bidRequest.root);
					System.out.println("--------------------------------------------");
				} catch (Exception error) {
error.printStackTrace();
				} */
			}
		}

	}
}
