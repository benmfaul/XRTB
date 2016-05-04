package com.xrtb.tools;

import java.io.BufferedReader;
import java.io.FileReader;
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
	static double COST = 3.0;

	static List<GeoStuff> geo = new ArrayList();

     static String HOST = "localhost";
	//static String HOST = "btsoomrtb";
	// static String HOST = "54.175.237.122";
	// static String HOST = "rtb4free.com";
	static String winnah = "__COST__/__LAT__/__LON__/__ADID__/__CRID__/__BIDID__/http://__HOST__:8080/contact.html?99201&adid=__ADID__&crid=__CRID__/http://__HOST__:8080/images/320x50.jpg?adid=__ADID__&__BIDID__";

	static String pixel = "/pixel/__EXCHANGE__/__ADID__/__CRID__/__BIDID__/__COST__/__LAT__/__LON__";

	static String redirect = "/redirect/__EXCHANGE__/__ADID__/__CRID__/__COST__/__LAT__/__LON__?url=http://__HOST__:8080/contact.html";
	
	static boolean COUNT_MODE = false;							// set false to replay a file

	public static void main(String[] args) throws Exception {		
		BufferedReader br = null;
		int percentWin = 80;
		int pixelPercent = 90;
		int clickPercent = 3;
		boolean forever = false;
		String fileName = "SampleBids/nexage.txt";
		String HOST = "rtb4free.com";
		ObjectMapper mapper = new ObjectMapper();
		int numberOfBids = 1000000;
		boolean silent = false;
		int sleepTime = 0;

		loadGeo();
		
		COUNT_MODE = true;
			
		int i = 0;
		while(i < args.length) {
			switch(args[i]) {
			case "-file":
				fileName = args[i+1];
				i+= 2;
				break;
			case "-host":
				HOST = args[i+1];
				i+= 2;
				break;
			case "-win":
				percentWin = Integer.parseInt(args[i+1]);
				i+= 2;
				break;
			case "-sleep":
				sleepTime = Integer.parseInt(args[i+1]);
				i+=2;
				break;
			case "-count":
				numberOfBids = Integer.parseInt(args[i+1]);
				COUNT_MODE = true;
				i+=2;
				break;
			case "-replay":
				COUNT_MODE=false;
				i++;
				break;
			case "-silent":
				silent = true;
				i++;
				break;
			case "-pixel":
				pixelPercent = Integer.parseInt(args[i+1]);
				i+=2;
				break;
			case "-click":
				clickPercent = Integer.parseInt(args[i+1]);
				i+=2;
				break;
			case "-forever":
				forever = true;
				i++;
				break;
			default:
				System.err.println("HUH?");
				System.exit(1);
			}
		}

		HttpPostGet post = new HttpPostGet();
		
		String data = null;
		
		if (COUNT_MODE) 
			data = new String(Files.readAllBytes(Paths
				.get(fileName)), StandardCharsets.UTF_8);
		else {
			br = new BufferedReader(new FileReader(fileName));
		}
		

		winnah = winnah.replaceAll("__HOST__", HOST);

		String bidURL = "http://" + HOST + ":8080/rtb/bids/__EXCHANGE__";
		String winURL = "http://" + HOST + ":8080/rtb/win/__EXCHANGE__/";
		String pixelURL = "http://" + HOST + ":8080" + pixel;

		String redirectURL = "http://" + HOST + ":8080" + redirect;
		redirectURL = redirectURL.replaceAll("__HOST__", HOST);

		int requests = 0, error = 0, bids = 0, wins = 0, pixels = 0, clicks = 0, nobid = 0;
		double bidCost = 0, winCost = 0;
		
		
		i = 0;
		boolean running = true;
		while(running == true) {
			i++;
			if (COUNT_MODE) {
				if (i >= numberOfBids) {
					running = false;
					break;
				}			
			}
			else {
				data = br.readLine();
				if (data == null) {
					if (forever) {          // start over
						br = new BufferedReader(new FileReader(fileName));
						data = br.readLine();
					} else {                // else all done
						running = false;
						break;
					}
				}
			}
			
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
					false);
			
			// System.out.println("----->" + data);
			Map map = mapper.readValue(data, Map.class);
			Map rets = randomize(map);
			
			boolean win = cointoss(percentWin);

			String thisBidUrl = bidURL;
			String thisWinUrl = winURL;
			String thisPixelURL = pixelURL;
			String exchange = getExchange();
			thisBidUrl = thisBidUrl.replaceAll("__EXCHANGE__", exchange);
			thisWinUrl = thisWinUrl.replaceAll("__EXCHANGE__", exchange);

				
			String bid = mapper.writeValueAsString(map);

			if (!silent) System.out.print(i + " --->");
			String hisBid = null;
			try {
				hisBid = post.sendPost(thisBidUrl, bid, 250, 250);

				if (sleepTime > 0)
					Thread.sleep(sleepTime);
				
				requests++;
				
				Thread.sleep(1);
				if (!silent) System.out.print("<---");
				if (hisBid != null) {
					bidCost += COST;
					bids++;
				}
				else
					nobid++;
				if (win && hisBid != null) {
					wins++;
					String theWin = makeWin(map, rets);
					
					String str = (String)map.get("cost");
					double wc = Double.parseDouble(str);

					
					if (!silent) System.out.print("W-->");
					String rc = post.sendGet(thisWinUrl + theWin, 5000, 5000);
					if (!silent) System.out.println(rc);
					winCost += wc;
					Thread.sleep(1);

					String crid = (String) map.get("crid");
					String adid = (String) map.get("adid");
					String cost = (String) map.get("cost");
					String bidid = (String) rets.get("uuid");
					String lat = "" + (Double)rets.get("lat");
					String lon = "" + (Double)rets.get("lon");
					
					if (cointoss(pixelPercent)) {

						thisPixelURL = thisPixelURL.replaceAll("__EXCHANGE__",
								exchange);
						thisPixelURL = thisPixelURL
								.replaceAll("__ADID__", adid);
						thisPixelURL = thisPixelURL
								.replaceAll("__CRID__", crid);
						thisPixelURL = thisPixelURL
								.replaceAll("__COST__", cost);
						thisPixelURL = thisPixelURL.replaceAll("__BIDID__",
								bidid);
						
						thisPixelURL = thisPixelURL.replaceAll("__LAT__",lat);
						thisPixelURL = thisPixelURL.replaceAll("__LON__",lon);

						rc = post.sendGet(thisPixelURL, 5000, 5000);
						
						pixels++;
					}

					if (cointoss(clickPercent)) {
						String thisRedirect = redirectURL.replaceAll(
								"__ADID__", adid);
						thisRedirect = thisRedirect.replaceAll("__BIDID__",
								bidid);
						thisRedirect = thisRedirect.replaceAll("__CRID__",
								crid);
						thisRedirect = thisRedirect.replaceAll("__EXCHANGE__",
								exchange);
						
						thisRedirect = thisRedirect.replaceAll("__COST__",
								"price="+cost);
						
						thisRedirect = thisRedirect.replaceAll("__LAT__",
								"lat="+lat);
						
						thisRedirect = thisRedirect.replaceAll("__LON__",
								"lon="+lon);
						
						rc = post.sendGet(thisRedirect, 5000, 5000);
						
						clicks++;
					}

				} else {
					if (!silent) System.out.println(".");
				}
			} catch (Exception err) {
			//	err.printStackTrace();
				error++;
			}

			// pixel load simulation here
			// click test here
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
		String data = new String(Files.readAllBytes(Paths
				.get("data/unique_geo_zipcodes.txt")), StandardCharsets.UTF_8);
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

	public static String makeWin(Map bid, Map r) {
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
			Map device = (Map)bid.get("device");
			if (device != null) {
				Map geo = (Map)device.get("geo");
				if (geo != null) {
					Double x = (Double)geo.get("lat");
					if (x != null) {
						lat = "" + x;
						x = (Double)geo.get("lon");
						lon = "" + x;
					}
				}
			}
			uuid = (String)bid.get("id");
		}
		cost = "" + (double) r.get("cost");

		/**
		 * Random cost
		 */
		Random rand = new Random();
		int Low = 760;
		int High = 1000;
		double Result = .001 * (rand.nextInt(High - Low) + Low);
		cost = "" + Result * COST;

		String adid = getAdId();
		String crid = getCrid();

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
		
		} else {
			uuid = (String)bid.get("id");
			r.put("uuid", uuid);
			r.put("lat", 0.0);
			r.put("lon",0.0);
			Map device = (Map) bid.get("device");
			if (device != null) {
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

	public static String getAdId() {
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

	public static String getCrid() {
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
	
		
		//if (1 == 1)
		//	return "smaato";
		
		
		if (k < 20) {
			return "smaato";
		}
		if (k < 50) {
			return "nexage";
		}
		if (k < 75) {
			return "fyber";
		}
		return "atomx";
	//	return "privatex";
	}

}

class GeoStuff {
	public String name;
	public double lat;
	public double lon;

	public GeoStuff() {

	}
}
