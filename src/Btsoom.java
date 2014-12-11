import com.xrtb.common.HttpPostGet;
import com.xrtb.common.Utils;


public class Btsoom {
	public static void main(String args[])   {
		HttpPostGet http = new HttpPostGet();
	
		try {
			String fake = Utils.readFile("smaato.json");
			String s = http.sendPost("http://rtb.btsoomllc.com:8080/rtb/bid/nexage", fake);
			System.out.println("X-time: " + http.getRunTime());
			System.out.println("Response code: " + http.getResponseCode());
			System.out.println(s);
			
			System.out.println("-------------------");
			s = http.sendPost("http://rtb.btsoomllc.com:8080/rtb/bid/nexage", fake);
			System.out.println("X-time: " + http.getRunTime());
			System.out.println("Response code: " + http.getResponseCode());
			System.out.println(s);

		} catch (Exception e) {
			e.printStackTrace();
		}
	} 
}
