import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.InputStream;

import javax.xml.bind.DatatypeConverter;

import com.xrtb.common.HttpPostGet;
import com.xrtb.pojo.BidRequest;

public class ReadInspectorLog {

	public static void main(String [] args) throws Exception {
		BufferedReader br = null;
		br = new BufferedReader(new FileReader("/home/ben/bin/inspector"));
		String data = null;
		HttpPostGet hp = new HttpPostGet();
		
		while((data=br.readLine()) != null) {
			data = data.replace("GOOD, DATA: ", "");
			BidRequest r = new BidRequest(new StringBuilder(data));
			
		}
	}
}
