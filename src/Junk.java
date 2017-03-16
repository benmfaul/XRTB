
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.bidder.RTBServer;
import com.xrtb.exchanges.adx.AdxBidRequest;
import java.util.Base64;

public class Junk {

	public static void main(String[] args) throws Exception {
		byte[] encryptionKey = {

				};
		
		byte[] integrityKey = {

				};
		String encoded = Base64.getEncoder().encodeToString(encryptionKey);
		System.out.println("E_KEY = " + encoded);
		encoded = Base64.getEncoder().encodeToString(integrityKey);
		System.out.println("I_KEY = " + encoded);
	}
}
