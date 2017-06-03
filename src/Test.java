import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.google.openrtb.OpenRtb.BidRequest;
import com.google.openrtb.json.OpenRtbJsonFactory;
import com.google.openrtb.json.OpenRtbJsonReader;
import com.xrtb.exchanges.google.GoogleBidRequest;

public class Test {

	public static void main(String args []) throws Exception {
		
		String test = "<img src=\\\"http://www/hello\" w=\\\"1\\\"";
		
		test = test.replaceAll("\\\\", "");
		
		System.out.println(test);
		
	}
}

class MyReader extends OpenRtbJsonReader {

	protected MyReader(OpenRtbJsonFactory factory) {
		super(factory);
		// TODO Auto-generated constructor stub
	}
	
}