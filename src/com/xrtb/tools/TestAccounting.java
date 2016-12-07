package com.xrtb.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
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

/**
 * Test program that generates bids/wins/clicks/pixels for the RTB4FREE server
 * to work with.
 * 
 * @author Ben M. Faul
 *
 */
public class TestAccounting {

	static double LAT = 42.378;
	static double LON = -71.227;
	static double COST = 3.0;

	static Random rdm = new Random();

	static List<GeoStuff> geo = new ArrayList();

	static String HOST = "localhost";
	// static String HOST = "btsoomrtb";
	// static String HOST = "54.175.237.122";
	// static String HOST = "rtb4free.com";
	static String winnah = "__COST__/__LAT__/__LON__/__ADID__/__CRID__/__BIDID__/http://__HOST__:8080/contact.html?99201&adid=__ADID__&crid=__CRID__/http://__HOST__:8080/images/320x50.jpg?adid=__ADID__&__BIDID__";

	static String pixel = "/pixel/__EXCHANGE__/__ADID__/__CRID__/__BIDID__/__COST__/__LAT__/__LON__";

	static String redirect = "/redirect/__EXCHANGE__/__ADID__/__CRID__/__COST__/__LAT__/__LON__?url=http://__HOST__:8080/contact.html";

	static boolean COUNT_MODE = false; // set false to replay a file

	public static void main(String[] args) throws Exception {
		BufferedReader br = null;
		int percentWin = 80;
		int pixelPercent = 90;
		int clickPercent = 3;
		boolean forever = false;
		String fileName = "SampleBids/nexage.txt";
		String HOST = "localhost:8080";
		ObjectMapper mapper = new ObjectMapper();
		int numberOfBids = 1000;
		boolean silent = false;
		int sleepTime = 0;
		String exchange = "nexage";

		loadGeo();

		COUNT_MODE = true;

		int i = 0;
		while (i < args.length) {
			switch (args[i]) {
			case "-h":
				System.out.println(
						"-file <filename>  [Set the file of the bid request(s) for the bidder, default SampleBids/nexage.txt]");
				System.out.println("-host <host:port> [Set the host:port for the bidder, default localhost:8080]");
				System.out.println("-win <n>          [Set the percentage to win, default 80]");
				System.out.println("-pixel <n>        [Set the percentage to pixel fires, default 90]");
				System.out.println("-click <n>        [Set the percentage to clicks, default 3]");
				System.out.println("-silent           [Don't show the transactions, default is to show them.");
				System.out.println("-count <n>        [Number of times to execute, default 1000000]");
				System.out.println("-forever          [loop on end of file on the file]");
			case "-file":
				fileName = args[i + 1];
				i += 2;
				break;
			case "-host":
				HOST = args[i + 1];
				i += 2;
				break;
			case "-exchange":
				exchange = args[i + 1];
				i += 2;
				break;
			case "-count":
				numberOfBids = Integer.parseInt(args[i + 1]);
				i+= 2;
				break;
			default:
				System.err.println("HUH: " + args[i]);
				System.exit(1);
			}
		}

		HttpPostGet post = new HttpPostGet();
		String data = null;
		br = new BufferedReader(new FileReader(fileName));

		winnah = winnah.replaceAll("__HOST__", HOST);

		String bidURL = "http://" + HOST + "/rtb/bids/__EXCHANGE__";
		String winURL = "http://" + HOST + "/rtb/win/__EXCHANGE__/";
		String pixelURL = "http://" + HOST + "/" + pixel;

		String redirectURL = "http://" + HOST + ":8080" + redirect;
		redirectURL = redirectURL.replaceAll("__HOST__", HOST);

		bidURL = bidURL.replaceAll("__EXCHANGE__", exchange);

		String thisWinUrl = winURL;
		thisWinUrl = thisWinUrl.replaceAll("__EXCHANGE__", exchange);

		int requests = 0, error = 0, bids = 0, wins = 0, pixels = 0, clicks = 0, nobid = 0;
		BigDecimal bidCost = new BigDecimal(0), winCost = new BigDecimal(0);

		Random r = new Random();

		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		String bid = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
		Map theBid = null;
		String hisBid = null;
		Map map = mapper.readValue(bid, Map.class);
		Map rets = randomize(map);
		double x = 0;

		for (i = 0; i < numberOfBids; i++) {
			String uuid = UUID.randomUUID().toString();
			try {
			String newBid = bid.replaceAll("35c22289-06e2-48e9-a0cd-94aeb79fab43",uuid);
			hisBid = post.sendPost(bidURL, newBid, 250, 250);
			} catch (Exception e) {
				error++;
				hisBid = null;
			}
			if (hisBid != null) {
				Map bidmap = mapper.readValue(hisBid, Map.class);
				List list = (List) bidmap.get("seatbid");
				bidmap = (Map) list.get(0);
				list = (List) bidmap.get("bid");
				theBid = (Map) list.get(0);
				x = (Double) theBid.get("price");
				bidCost = bidCost.add(new BigDecimal(x));
				bids++;
			} else
				nobid++;
			if (hisBid != null) {
				wins++;
				String str = "" + x; // (String)map.get("cost");
				BigDecimal wc = new BigDecimal(str);
				winCost = winCost.add(wc);
				rets.put("uuid", uuid);
				String theWin = makeWin(map, theBid, rets, wc.doubleValue());
				String rc = post.sendGet(thisWinUrl + theWin, 5000, 5000);
				if (rc == null || rc.length() == 0)
					System.err.println("Bad Win return");
			}
			System.out.println(i);
			requests++;
		}

		System.out.println("Requests = " + requests);
		System.out.println("Bids = " + bids);
		System.out.println("Nobids = " + nobid);
		System.out.println("Wins = " + wins);
		System.out.println("Pixels = " + pixels);
		System.out.println("Clicks = " + clicks);
		System.out.println("BID COST = " + bidCost);
		System.out.println("WIN COST = " + winCost);
		System.out.println("Errors: " + error);
	}

	public static void loadGeo() throws Exception {
		String data = new String(Files.readAllBytes(Paths.get("data/unique_geo_zipcodes.txt")), StandardCharsets.UTF_8);
		String[] lines = data.split("\n");
		for (String line : lines) {
			String parts[] = line.split(",");
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

	public static String makeWin(Map bid, Map theBid, Map r, double dcost) {
		String str = winnah;
		String lat = "NA";
		String lon = "NA";
		String cost = "";
		String uuid = "";

		if (COUNT_MODE) {
			lat = "" + (double) r.get("lat");
			lon = "" + (double) r.get("lon");
			uuid = (String) r.get("uuid");
		} else {
			Map device = (Map) bid.get("device");
			if (device != null) {
				Map geo = (Map) device.get("geo");
				if (geo != null) {
					Double x = (Double) geo.get("lat");
					if (x != null) {
						lat = "" + x;
						x = (Double) geo.get("lon");
						lon = "" + x;
					}
				}
			}
			uuid = (String) bid.get("id");
		}
		cost = "" + dcost;

		/**
		 * Random cost
		 */
		Random rand = new Random();
		int Low = 760;
		int High = 1000;
		double Result = .001 * (rand.nextInt(High - Low) + Low);
		// cost = "" + Result * COST;

		String adid = (String) theBid.get("adid");
		String crid = (String) theBid.get("crid");

		str = str.replaceAll("__LAT__", lat);
		str = str.replaceAll("__LON__", lon);
		str = str.replaceAll("__COST__", cost);
		str = str.replaceAll("__BIDID__", uuid);
		str = str.replaceAll("__ADID__", adid);
		str = str.replaceAll("__CRID__", crid);

		bid.put("adid", adid);
		bid.put("crid", crid);
		bid.put("cost", cost);

		return str;

	}

	public static Map randomize(Map bid) {
		GeoStuff q = null;
		Map r = new HashMap();
		String uuid = null;
		String ip = rdm.nextInt(256) + "." + rdm.nextInt(256) + "." + rdm.nextInt(256) + "." + rdm.nextInt(256);
		if (COUNT_MODE) {
			uuid = UUID.randomUUID().toString();
			bid.put("id", uuid);
			Map device = (Map) bid.get("device");
			Map geo = (Map) device.get("geo");
			q = randomGeo();
			geo.put("lat", q.lat);
			geo.put("lon", q.lon);
			r.put("lat", q.lat);
			r.put("lon", q.lon);
			r.put("uuid", uuid);

			device.put("ip", ip);

		} else {
			uuid = (String) bid.get("id");
			r.put("uuid", uuid);
			r.put("lat", 0.0);
			r.put("lon", 0.0);
			Map device = (Map) bid.get("device");
			if (device != null) {
				device.put("ip", ip);
				Map geo = (Map) device.get("geo");
				if (geo != null) {
					if (geo.get("lat") != null) {
						r.put("lat", geo.get("lat"));
						r.put("lon", geo.get("lon"));
					}
				}
			}
		}
		r.put("cost", COST);
		return r;
	}

	public static boolean cointoss(int chance) {
		Random r = new Random();
		int Low = 1;
		int High = 100;
		int Result = r.nextInt(High - Low) + Low;
		if (Result < chance)
			return true;
		else
			return false;
	}

	public static String getAdId(Map bid) {
		Random r = new Random();
		int Low = 1;
		int High = 100;
		int k = r.nextInt(High - Low) + Low;
		if (k < 10) {
			return "ben:payday";
		}
		if (k < 50) {
			// return "peter:payday";
		}
		if (k < 80) {
			// return "jim:payday";
			return "ben:payday";
		}
		return "ben:payday";
		// return "ford";
	}

	public static String getCrid(Map bid) {
		Random r = new Random();
		int Low = 1;
		int High = 100;
		int k = r.nextInt(High - Low) + Low;
		if (k < 50) {
			// return "creative-100";
			return "23skiddoo";
		}
		if (k < 75) {
			// return "creative-22";
			return "23skiddoo";
		}
		return "23skiddoo";
		// return "creative-99";
	}

	public static String getExchange() {
		Random r = new Random();
		int Low = 1;
		int High = 100;
		int k = r.nextInt(High - Low) + Low;

		// if (1 == 1)
		// return "smaato";

		if (k < 10) {
			return "kadam";
		}

		if (k < 20) {
			return "citenko";
		}

		if (k < 30) {
			return "smaato";
		}

		if (k < 40) {
			return "epomx";
		}

		if (k < 50) {
			return "nexage";
		}
		if (k < 75) {
			return "fyber";
		}
		return "atomx";
		// return "privatex";
	}

}

class XGeoStuff {
	public String name;
	public double lat;
	public double lon;

	public XGeoStuff() {

	}
}
