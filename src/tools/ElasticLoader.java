package tools;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.common.HttpPostGet;

public class ElasticLoader {

	static double LAT = 42.378;
	static double LON = -71.227;
	static double COST = .01;

	//static String HOST = "rtb4free.com";
	
	static List<GeoStuff> geo = new ArrayList();
	
	static String HOST = "localhost";
	
	//static String HOST = "54.175.237.122";

	static String winnah = "__COST__/__LAT__/__LON__/__ADID__/__BIDID__/http://__HOST__:8080/contact.html?99201&adid=__ADID__&crid=__CRID__/http://__HOST__:8080/images/320x50.jpg?adid=__ADID__&__BIDID__";

	public static void main(String[] args) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		int numberOfBids = 100000;

		if (args.length != 0)
			HOST = args[0];
		
		loadGeo();
		
		int percentWin = 80;
		HttpPostGet post = new HttpPostGet();
		String data = new String(Files.readAllBytes(Paths
				.get("SampleBids/nexage.txt")), StandardCharsets.UTF_8);

		winnah = winnah.replaceAll("__HOST__", HOST);

		String bidURL = "http://" + HOST + ":8080/rtb/bids/__EXCHANGE__";
		String winURL = "http://" + HOST + ":8080/rtb/win/__EXCHANGE__/";

		for (int i = 0; i < numberOfBids; i++) {
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
					false);
			Map map = mapper.readValue(data, Map.class);
			Map rets = randomize(map);
			boolean win = cointoss(i, percentWin);

			String thisBidUrl = bidURL;
			String thisWinUrl = winURL;
			String exchange = getExchange();
			thisBidUrl = thisBidUrl.replaceAll("__EXCHANGE__", exchange);
			thisWinUrl = thisWinUrl.replaceAll("__EXCHANGE__", exchange);

			String bid = mapper.writeValueAsString(map);
			
			System.out.print(i +" --->");
			String hisBid = null;
			try {
				hisBid = post.sendPost(thisBidUrl, bid,5000,5000);
			} catch (Exception error) {
				Thread.sleep(1000);
				post = new HttpPostGet();
				System.out.println("!!!");
				hisBid = post.sendPost(thisBidUrl, bid, 5000, 5000);
			}
			Thread.sleep(1);
			System.out.print("<---");
			if (win  && hisBid != null) {
				String theWin = makeWin(map, rets);
				System.out.print("W-->");
				String rc = post.sendGet(thisWinUrl + theWin, 5000,5000);
				System.out.println(rc);
				Thread.sleep(1);
			} else {
				System.out.println(".");
			}

			// pixel load simulation here
			// click test here
		}
	}
	
	public static void loadGeo() throws Exception {
		String data = new String(Files.readAllBytes(Paths
				.get("data/unique_geo_zipcodes.txt")), StandardCharsets.UTF_8);
		String [] lines = data.split("\n");
		for (String line : lines) {
			String parts [] = line.split(",");
			GeoStuff q = new GeoStuff();
			q.name = parts[0];
			q.lat = Double.parseDouble(parts[1]);
			q.lon = Double.parseDouble(parts[2]);
			geo.add(q);
		}
	}
	
	public static GeoStuff randomGeo() {
		GeoStuff q = null;
		Random rand = new Random();
		int Low = 0;
		int High = geo.size();
		int Result = rand.nextInt(High - Low) + Low;
		return geo.get(Result);
		
	}

	public static String makeWin(Map bid, Map r) {
		String str = winnah;
		
		String lat = "" + (double) r.get("lat");
		String lon = "" + (double) r.get("lon");
		String cost = "" + (double) r.get("cost");
		String uuid = (String) r.get("uuid");

		/** 
		 * Random cost
		 */
		Random rand = new Random();
		int Low = 760;
		int High = 1000;
		double Result = .001 * (rand.nextInt(High - Low) + Low);
		cost = "" + Result * COST;
		
		str = str.replaceAll("__LAT__", lat);
		str = str.replaceAll("__LON__", lon);
		str = str.replaceAll("__COST__", cost);
		str = str.replaceAll("__BIDID__", uuid);
		str = str.replaceAll("__ADID__", getAdId());
		str = str.replaceAll("__CRID__", getCrid());

		return str;

	}

	public static Map randomize(Map bid) {
		String uuid = UUID.randomUUID().toString();
		bid.put("id", uuid);
		Map device = (Map) bid.get("device");
		Map geo = (Map) device.get("geo");

		Map r = new HashMap();
		
		GeoStuff q = randomGeo();
		
		geo.put("lat",q.lat);
		geo.put("lon", q.lon);
		
		
		r.put("uuid", uuid);
		r.put("lat", q.lat);
		r.put("lon", q.lon);
		r.put("cost", COST);
		
		/**
		 * Random request for invalid size 
		 * 
		 */
		Random rand = new Random();
		int Low = 1;
		int High = 100;
		int x = rand.nextInt(High - Low) + Low;
		if (x < 25) {
			List list = (List)bid.get("imp");
			Map m = (Map)list.get(0);
			m = (Map)m.get("banner");
			m.put("w",1000);		
		} 

		return r;
	}

	public static boolean cointoss(int i, int chance) {
		Random r = new Random();
		int Low = 1;
		int High = 100;
		int Result = r.nextInt(High - Low) + Low;
		if (Result < chance)
			return true;
		else
			return false;
	}

	public static String getAdId() {
		Random r = new Random();
		int Low = 1;
		int High = 100;
		int k = r.nextInt(High - Low) + Low;		
		if (k < 10) {
			return "ben:payday";
		}
		if (k < 50) {
			return "peter:payday";
		}
		if (k < 80) {
			return "jim:payday";
		}
		return "ford";
	}

	public static String getCrid() {
		Random r = new Random();		
		int Low = 1;
		int High = 100;
		int k = r.nextInt(High - Low) + Low;
		if (k < 50) {
			return "creative-100";
		}
		if (k < 75) {
			return "creative-22";
		}
		return "creative-99";
	}

	public static String getExchange() {
		Random r = new Random();
		int Low = 1;
		int High = 100;
		int k = r.nextInt(High - Low) + Low;	
		if (k < 50) {
			return "nexage";
		}
		if (k < 75) {
			return "fyber";
		}
		return "privatex";
	}

}

class GeoStuff {
	public String name;
	public double lat;
	public double lon;
	
	public GeoStuff() {
		
	}
}
