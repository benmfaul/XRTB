package com.xrtb.tools.explorer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A quick and dirty requst map reduce scanner, for use with archived JSON records of the bid requests
 * @author Ben M. Faul
 *
 */
public class RequestScanner {

	static Map<String, Integer> allIabs = new HashMap();
	static Map<String, Integer> blocked = new HashMap();
	static Map<String, Integer> domains = new HashMap();
	static Map<String, Integer> banners = new HashMap();
	static Map<String, Integer> btype = new HashMap();
	static Map<String, Integer> countries = new HashMap();
	static Map<String, Integer> oss = new HashMap();
	static Map<String, Integer> carriers = new HashMap();
	static Map<String, Integer> makes = new HashMap();
	static Map<String, Integer> models = new HashMap();
	
	static Map<String,String> blockType = new HashMap();
	static {
		blockType.put("1","Html Text Ad");
		blockType.put("2","Html Banner Ad");
		blockType.put("3","JavaScript");
		blockType.put("4","IFrame");
	}

	public static ObjectMapper mapper = new ObjectMapper();

	public static void main(String[] args) throws Exception {
		String fin = "logs/request";
		//String fin = "logs/request";
		int i = 0;
		//int w = 320;
		//int h = 50;
		int w = 320;
		int h = 50;
		
		while(i<args.length) {
			switch(args[i]) {
			case "-w":
				w = Integer.parseInt(args[i+1]);
				i+= 2;
				break;
			case "-h":
				h = Integer.parseInt(args[i+1]);
				i+= 2;
				break;
			}
		}
		BufferedReader br = new BufferedReader(new FileReader(fin));

		String line = null;
		Map map = null;
		double count = 0;
		double instlCount = 0;
		double bidFloorCount = 0;
		double bannerCount = 0;
		double bannerWithFloor = 0;
		double bidFloorValue = 0;
		double bannerFloorValue = 0;
		double ourSize = 0;
		double ourSizeBidFloor = 0;
		double mimeCount = 0;
		double apiCount = 0;
		double apiFloorValue = 0;
		double nonApiFloorValue = 0;
		double geoCount = 0;
		double ourGeoCount = 0;
		double noCat = 0;
		double noBcat = 0;
		double bCat = 0;
		double countryCount = 0;
		double osCount = 0;
		double carrierCount = 0;
		double videoCount = 0;
		double nativeCount = 0;
		double makeCount = 0;
		double modelCount = 0;
		
		double btypeCount = 0;
		
		while ((line = br.readLine()) != null) {
			boolean us = false;
			count++;
			map = mapper.readValue(line, Map.class);
			List imp = (List) map.get("imp");
			
			
			Map x = (Map) imp.get(0);
			
			if (x.get("instl") != null) {
				Integer d = (Integer)x.get("instl");
				if (d != 0)
					instlCount++;
			}
			
			Double v = null;
			Object q = x.get("bidfloor");
			if (q instanceof Integer) {
				Integer qq = (Integer) x.get("bidfloor");
				v = new Double(qq.doubleValue());
			} else
				v = (Double) x.get("bidfloor");

			Map banner = (Map) x.get("banner");
			
			Map video = (Map)x.get("video");
			Map nativeAd = (Map)x.get("native");
			
			if (video != null)
				videoCount++;
			if (nativeAd != null)
				nativeCount++;

			Map device = (Map) map.get("device");
			Map geo = null;
			if (device != null) {
				
				
				String make = (String)device.get("make");
				if (make != null) {
					makeCount++;
					Integer mK = makes.get(make);
					if (mK == null) {
						mK = new Integer(0);
					}
					mK++;
					makes.put(make,mK);
				}
				
				String model = (String)device.get("mode;");
				if (model != null) {
					modelCount++;
					Integer mK = models.get(make);
					if (mK == null) {
						mK = new Integer(0);
					}
					mK++;
					models.put(make,mK);
				}
				
				String carrier = (String)device.get("carrier");
				if (carrier != null) {
					carrierCount++;
					Integer cK = carriers.get(carrier);
					if (cK == null) {
						cK = new Integer(0);
					}
					cK++;
					carriers.put(carrier,cK);
				}
				
				String os = (String)device.get("os");
				if (os != null) {
					osCount++;
					Integer osK = oss.get(os);
					if (osK == null) {
						osK = new Integer(0);
					}
					osK++;
					oss.put(os, osK);
				}
				
				geo = (Map) device.get("geo");
				if (geo != null) {
					Double lat = (Double) geo.get("lat");
					String country = (String)geo.get("country");
					if (country != null) {
						countryCount++;
						Integer mc = countries.get(country);
						if (mc == null) {
							mc = new Integer(0);	
						}
						mc++;
						countries.put(country,mc);
					}
					
					if (lat == null || lat == 0 || country == null)
						geo = null;
				}
			}
			if (banner != null) {
				bannerCount++;
				
				Integer wx = (Integer) banner.get("w");
				Integer hx= (Integer) banner.get("h");

				String test = "" + wx + ":" + hx;
				Integer szcount = (Integer)banners.get(test);
				if (szcount == null)
					szcount = new Integer(0);
				szcount++;
				banners.put(test,szcount);
				
				List<Integer> bt;
				Object what = banner.get("btype");
				if (what instanceof Integer) {
					bt = new ArrayList();
					bt.add((Integer)what);
				} else {
				
				bt = (List)banner.get("btype");
				if (bt != null) {
					btypeCount++;
					for (Integer z : bt) {
						test = "" + z;
						szcount = (Integer)btype.get(test);
						if (szcount == null)
							szcount = new Integer(0);
						szcount++;
						btype.put(test,szcount);
					}
				}
				}
				
				if (wx == w && h == hx) {
					ourSize++;
					if (v != null)
						ourSizeBidFloor += v;
					if (geo != null)
						ourGeoCount++;

					List api = (List) banner.get("api");
					if (api != null) {
						apiCount++;
						if (v != null)
						apiFloorValue += v;
					} else {
						if (v != null)
							nonApiFloorValue += v;
						us = true;
					}
				}
				List mimes = (List) banner.get("mimes");
				if (mimes != null) {
					mimeCount++;
				}
			}

			if (geo != null)
				geoCount++;

			if (v != null) {
				bidFloorCount++;
				bidFloorValue += v;
				if (banner != null) {
					bannerFloorValue += v;
				}
			}

			Map site = (Map) map.get("site");
			if (site != null) {

				String domain = (String) site.get("domain");
				Integer domcount = domains.get(domain);

				if (domcount == null) {
					domcount = new Integer(0);
				}
				domcount++;
				domains.put(domain, domcount);

				Object obj = site.get("cat");
				if (obj instanceof String) {
					String s =(String)obj;
					String [] list = s.split(",");
					for (String iab : list) {
						int k = iab.indexOf("-");
						if (k > 0) {
							iab = iab.substring(0, k);
						}
						Integer what = (Integer) allIabs.get(iab);
						if (what == null) {
							what = new Integer(0);
						}
						what++;
						allIabs.put(iab, what);
					}
					
				} else {
				List<String> list = (List) site.get("cat");
				if (list != null) {
					for (String iab : list) {
						int k = iab.indexOf("-");
						if (k > 0) {
							iab = iab.substring(0, k);
						}
						Integer what = (Integer) allIabs.get(iab);
						if (what == null) {
							what = new Integer(0);
						}
						what++;
						allIabs.put(iab, what);
					}
				} else
					noCat++;
				}
			}

			List<String> bcat = (List) map.get("bcat");
			if (bcat != null) {
				if (bcat != null) {
					bCat++;
					for (String iab : bcat) {
						int k = iab.indexOf("-");
						if (k > 0) {
							iab = iab.substring(0, k);
						}
						Integer what = (Integer) blocked.get(iab);
						if (what == null) {
							what = new Integer(0);
						}
						what++;
						blocked.put(iab, what);
					}
				}
			} else {
				noBcat++;
			}

		// System.out.println(line);

		}

		double weBid = ourSize - apiCount;
		System.out.println("Count = " + count 
				+ "\nInstl = " + instlCount + ", (" + (instlCount / count * 100.0) + ")"
				+ "\nBid Floor = "
				+ bidFloorCount + ", (" + (bidFloorCount / count * 100.0) + ")"
				+ "\nAvg Bid Floor = " + (bidFloorValue / bidFloorCount)
				+ "\n\nBanners = " + bannerCount + ", ("
				+ (bannerCount / count * 100) + ")"
				
				+ "\nVideo = " + videoCount + ", ("
				+ (videoCount / count * 100) + ")"
				
				+ "\nNative = " + nativeCount + ", ("
				+ (nativeCount / count * 100) + ")"
				
				
				+ "\n\nAvg Banner Bid Floor = " + bannerFloorValue / bannerCount
				+ "\nOur Size Banners = " + ourSize + ", ("
				+ (ourSize / count * 100) + ")" + "\nAvg Our Size Bid Floor = "
				+ (ourSizeBidFloor / ourSize) +

				"\nMimes of Our Size = " + mimeCount + ", ("
				+ (mimeCount / ourSize * 100) + ")" + "\nAPIs of Our Size = "
				+ apiCount + ", (" + (apiCount / ourSize * 100) + ")" +

				"\nAvg Non API Bid Floor = "
				+ (nonApiFloorValue / (ourSize - apiCount))
				+ "\nAvg API Bid Floor = " + (apiFloorValue / (apiCount)) +

				"\nGeo = " + geoCount + ", (" + (geoCount / count * 100) + ")"
				+ "\nOur Geo = " + (ourGeoCount / ourSize * 100) +

				"\n");

		List<Quartet> qlist = reduceQuartet(allIabs,(int)count);
		System.out.println("Not categorized: " + noCat + " ("
				+ (noCat / count * 100) + ")");
		qlist.forEach((q) -> System.out.printf("%s, %d, (%.3f%%) %s\n",q.iab,q.count,q.percent,q.description));

		// ////////////////////////////////////////

		System.out.println("\nBlocked Categories\n");
		qlist = reduceQuartet(blocked,(int)count);
		qlist.forEach((q) -> System.out.printf("%s, %d, (%.3f%%) %s\n",q.iab,q.count,q.percent,q.description));

		// ///////////////////////

		System.out.println("\n\nSite analysis\n\n");
		List<Tuple>tups = reduce(domains,(int)count);
		tups.forEach((q) -> System.out.printf("%s, %d, (%.3f%%)\n",q.site,q.count,q.percent));
		
		System.out.println("\n\nBanner Sizes\n\n");
		tups = reduce(banners,(int)bannerCount);
		tups.forEach((q) -> System.out.printf("%s, %d, (%.3f%%)\n",q.site,q.count,q.percent));

		//////////////////////// blocked banner types ///////////////////////////
		
		System.out.println("\n\nBlocked Banner Type\n\n");
		tups = reduce(btype,(int)bannerCount);
		tups.forEach((q) -> System.out.printf("%s (%s), %d, (%.3f%%)\n",q.site,blockType.get(q.site),q.count,q.percent));
	
		
		System.out.println("\n\nCountries\n");
		tups = reduce(countries,(int)countryCount);
		tups.forEach((q) -> System.out.printf("%s, %d, (%.3f%%)\n",q.site,q.count,q.percent));
		
		System.out.println("\n\nDevice OS\n");
		tups = reduce(oss,(int)osCount);
		tups.forEach((q) -> System.out.printf("%s, %d, (%.3f%%)\n",q.site,q.count,q.percent));
		
		System.out.println("\n\nCarriers, Percent with Carrier Data = " + ((Double)carrierCount/count * 100) + "\n");
		tups = reduce(carriers,(int)carrierCount);
		tups.forEach((q) -> System.out.printf("%s, %d, (%.3f%%)\n",q.site,q.count,q.percent));
		
		
		System.out.println("\n\nMake, Percent with Make Data = " + ((Double)makeCount/count * 100) + "\n");
		tups = reduce(makes,(int)makeCount);
		tups.forEach((q) -> System.out.printf("%s, %d, (%.3f%%)\n",q.site,q.count,q.percent));
		
		System.out.println("\n\nModel, Percent with Model Data = " + ((Double)modelCount/count * 100) + "\n");
		tups = reduce(models,(int)modelCount);
		tups.forEach((q) -> System.out.printf("%s, %d, (%.3f%%)\n",q.site,q.count,q.percent));
		
		br.close();
	}
	
	public static  List<Quartet> reduceQuartet(Map<String,Integer> map, int count) {
		Iterator<String> it = map.keySet().iterator();
		List<Quartet> iabs = new ArrayList();
		while (it.hasNext()) {
			String key = it.next();
			Integer value = map.get(key);
			Quartet q = new Quartet(key, value, (int) count);
			iabs.add(q);
		}
		Collections.sort(iabs);
		return iabs;
	}
	
	public static List<Tuple> reduce(Map data, int count) {
		Iterator itx = data.keySet().iterator();
		List<Tuple> tups = new ArrayList();
		Collections.sort(tups);
		while (itx.hasNext()) {
			String key =(String) itx.next();
			Integer value = (Integer)data.get(key);
			Tuple q = new Tuple(key, value, count);
			tups.add(q);
		}
		Collections.sort(tups);
		return tups;
	}
}



