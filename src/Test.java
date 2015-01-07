import java.io.IOException;

import org.codehaus.jackson.JsonProcessingException;

import com.xrtb.exchanges.Nexage;
import com.xrtb.pojo.BidRequest;


public class Test {

	public static void main(String []args) throws JsonProcessingException, IOException {
		Nexage n = new Nexage();
		BidRequest x = n.copy(null);
		
	}
}
