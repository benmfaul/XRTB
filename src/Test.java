import java.io.IOException;

import org.codehaus.jackson.JsonProcessingException;

import com.xrtb.exchanges.Nexage;
import com.xrtb.pojo.BidRequest;


/**
 * String indexOf is faster than StringBuffer
 * Replace, StringBuffer much faster, at leat 10x
 * @author ben
 *
 */
public class Test {

	public static void main(String []args) throws JsonProcessingException, IOException {

		String s = "%7Bad_id%7D";
		s = s.replace("%7Bad_id%7D","XXX");
		System.out.println(s);
		
	}
}
