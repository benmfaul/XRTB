package tools;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
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

	static String HOST = "rtb4free.com";

	static String winnah = "__COST__/__LAT__/__LON__/__ADID__/__BIDID__/http://__HOST__:8080/contact.html?99201&adid=__ADID__&crid=__CRID__/http://__HOST__:8080/images/320x50.jpg?adid=__ADID__&__BIDID__";

	public static void main(String[] args) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		int numberOfBids = 100000;

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
			String hisBid = post.sendPost(thisBidUrl, bid);
			if (win) {
				String theWin = makeWin(map, rets);
				String rc = post.sendGet(thisWinUrl + theWin);
			}

			// pixel load simulation here
			// click test here
		}
	}

	public static String makeWin(Map bid, Map r) {
		String str = winnah;
		String lat = "" + (double) r.get("lat");
		String lon = "" + (double) r.get("lon");
		String cost = "" + (double) r.get("cost");
		String uuid = (String) r.get("uuid");

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
		r.put("uuid", uuid);
		r.put("lat", LAT);
		r.put("lon", LON);
		r.put("cost", COST);

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