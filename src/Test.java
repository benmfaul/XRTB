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
		
		JsonFactory jfactory = new JsonFactory();
		File initialFile = new File("SampleBids/atomx.txt");
	    InputStream targetStream = new FileInputStream(initialFile);
		JsonParser parser =jfactory.createJsonParser(initialFile);
		
		OpenRtbJsonFactory jf = OpenRtbJsonFactory.create();
		
		MyReader reader = new MyReader(jf);
		BidRequest r = reader.readBidRequest(targetStream);
		
		GoogleBidRequest google = new GoogleBidRequest(r);
		System.out.println(r);
		System.out.println(google.toString());
		
	}
}

class MyReader extends OpenRtbJsonReader {

	protected MyReader(OpenRtbJsonFactory factory) {
		super(factory);
		// TODO Auto-generated constructor stub
	}
	
}