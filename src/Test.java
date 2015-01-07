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

		String s = "abcde";
		String xx = "abcd";
		
		boolean b = false;
		long time = System.currentTimeMillis();
		for (int i=0;i<10000000;i++) {
			s.equals(xx);
		}
		time = System.currentTimeMillis() - time;
		System.out.println(""+b+" = " + time);
		
		StringBuffer bs = new StringBuffer(s);
		StringBuffer bx = new StringBuffer(xx);
		time = System.currentTimeMillis();
		for (int i=0;i<10000000;i++) {
			bs.equals(bx);
		}
		time = System.currentTimeMillis() - time;
		System.out.println(""+b+" = " + time);
		
	}
}
