import redis.clients.jedis.Jedis;


public class TestQuote {

	public static void main(String args[]) throws Exception {
		

		Jedis jedis  = new Jedis("localhost");
		jedis.connect();
		
		String forwardurl = jedis.get("a");
		
		System.out.println("Hello: " + forwardurl);
		/*
		 * Encode JavaScript tags
		 */
		
		if (forwardurl.contains("<script") || forwardurl.contains("<SCRIPT")) {
			if (forwardurl.contains("\"") && (forwardurl.contains("\\\"") == false)) {
				forwardurl = forwardurl.replaceAll("\"", "\\\\\"");
			}
		}
		
		System.out.println("FINAL: " + forwardurl);
		
		
	}
}
