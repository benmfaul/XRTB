import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.bidder.RTBServer;
import com.xrtb.exchanges.adx.AdxBidRequest;

public class AdxStuff {

	public static void main (String args[]) throws Exception {
		BufferedReader br = null;
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				false);
		String data = null;
		
		new RTBServer("./Campaigns/payday.json");
		
		Map ext = new HashMap();
		ext.put("e_key","Q4RKDXxA7HXG7qzxa3pFSu1rIWH1RuQ/3FAcUKgL3/Y=");
		ext.put("i_key", "OzE5pRCwvuNzNZer3Cpzkj7zMWuvgNf5DzsjpGlET68=");
		new AdxBidRequest().handleConfigExtensions(ext);
		
		br = new BufferedReader(new FileReader("/media/twoterra/adxrequest"));
		while((data=br.readLine()) != null) {
			Map map = mapper.readValue(data, Map.class);
			String protobuf = (String)map.get("protobuf");
			byte [] protobytes = DatatypeConverter.parseBase64Binary(protobuf);
			InputStream is = new ByteArrayInputStream(protobytes);
			AdxBidRequest bidRequest = new AdxBidRequest(is);
			System.out.println(protobuf);
		}
		
	}
}
