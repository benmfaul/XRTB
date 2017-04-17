package com.xrtb.geo;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class that does specialized geo tagging. Given a GPS coordinate, returns the closest US zip code.
 * @author Ben M. Faul
 *
 */
public class GeoTag {

	/** Earth radius in km */
	public final double ERADIUS = 6371;
	/** PI to 5 digits */
	public final double PI = 3.14159;

	Map<String, List<Solution>> table = new HashMap();
	Map<Integer, List<String>> zipStates = new HashMap();

	public GeoTag() {
		
	}
	
	/**
	 * Loads the database or zip codes and zip code states.
	 * @param zipState String. The path of the zip codes in state database 
	 * @param datafile String. The path of the zip code cestroids 
	 * @throws Exception on file errors 
	 */
	public void initTags(String zipState, String datafile) throws Exception {
		if (zipState == null || zipState.length() == 0)
			return;
		if (datafile == null || datafile.length() == 0)
			return;
		
		loadZipMap(zipState);
		loadDatabase(datafile);
	}

	/**
	 * Loads the zipcode centroids/
	 * @param path String. The path of the zip code geo codes 
	 * @throws Exception on file errors.
	 */
	public void loadDatabase(String path) throws Exception {
		double a, b;
		int i = 0, c = 0;
		String key = null, sa = null;
		double nkey = 0;
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		String str = Charset.defaultCharset().decode(ByteBuffer.wrap(encoded))
				.toString();
		String lines[] = str.split("\n");
		for (String myline : lines) {
			String items[] = myline.split(",");
			sa = items[0];
			c = Integer.parseInt(sa);
			a = Double.parseDouble(items[1]);
			nkey = a;
			a /= PI / 180;
			b = Double.parseDouble(items[2]);
			double lonb = b;
			b *= PI / 180;

			key = makeKey(nkey);
			List<Solution> s = table.get(key);
			if (s == null) {
				s = new ArrayList<Solution>();
				table.put(key, s);
			}
			Solution sol = new Solution();
			sol.code = c;
			sol.lon = lonb;
			List<String> v = zipStates.get(c);
			int j = 0;
			if (v != null) {
				for (String what : v) {
					if (j == 3)
						sol.city = what;
					if (j == 4)
						sol.state = what;
					if (j == 5)
						sol.county = what;
					j++;
				}
			}
			s.add(sol);
		}
	}

	/**
	 * Loads the zip code per state map
	 * @param path String. The path name of the zip codes in state file
	 * @throws Exception on file errors 
	 */
	public void loadZipMap(String path) throws Exception {
	     String line, csvItem, sa;
	     double a, b;
	     int i = 0;
		String items[];
		List<String> list = null;;

		double nkey = 0;
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		String str = Charset.defaultCharset().decode(ByteBuffer.wrap(encoded))
				.toString();
	  
		String [] lines = str.split("\n");

		for (int k=1;k<lines.length;k++) {
			items = lines[k].split(",");
			list = new ArrayList<String>();
			for (int j=0;j<items.length;j++) {
				items[j] = items[j].replaceAll("\"", "");
			}
			items[0] = items[0].replaceFirst ("^0*", "");
			if (items.length != 6) {
				list.add(items[0]);
				list.add("0");
				list.add("0");
				list.add("0");
				list.add(items[1]);
				list.add(items[2]);
			} else {
				for (i=0;i<6;i++) {
					list.add(items[i]);
				}
			}

			int d = Integer.parseInt(items[0]);
			zipStates.put(d,list);
		}
	}

	/**
	 * Given a GPS coordinate, return the solutin (zipcode, state, county, and city).
	 * @param lat double. The GPS latitude.
	 * @param lon double. The GPS longitude
	 * @return Solution. Where this GPS location is.
	 */
	public Solution getSolution(double lat, double lon) {
		double d = 0;
		String key = makeKey(lat);
		//key = "33.75";
		List<Solution> ps = table.get(key);
		if (ps == null)
			return null;
		Solution bestSolutions = null;
		Solution sol = null;
		double dist = 1000000000;
		for (Solution it : ps) {
			sol = it;
			double longitude = sol.lon;
			double test = getRange(lat,lon,lat,longitude);
			if (test < dist) {
				dist = test;
				bestSolutions = it;
			}
		}
		return bestSolutions;
	}

	/**
	 * Returns the range in km between two GPS points.
	 * @param lat1 double. The latitiude of the first point.
	 * @param long1 double. The longitude of the first point.
	 * @param lat2 double. The latitude of the second point.
	 * @param long2 double. The longitude of the second point.
	 * @return double. The range in km.
	 */
	public double getRange(double lat1, double long1, double lat2, double long2) {
		 double dlat1=lat1*(PI/180);

		 double dlong1=long1*(PI/180);
		 double dlat2=lat2*(PI/180);
		 double dlong2=long2*(PI/180);

		 double dLong=dlong1-dlong2;
	         double dLat=dlat1-dlat2;

		 double aHarv= Math.pow(Math.sin(dLat/2.0),2.0)+Math.cos(dlat1)*Math.cos(dlat2)*Math.pow(Math.sin(dLong/2),2);
	     double cHarv=2*Math.atan2(Math.sqrt(aHarv),Math.sqrt(1.0-aHarv));
		 double earth=6378.137*1000; // meters
		return earth*cHarv;
	}

	/**
	 * Makes a string key of 2 digits accuracy. Example 74.2344 is converted to '74.23'
	 * @param d double. The value to turn into a key.
	 * @return String. The string representation of the key value of 'd' to 2 digits (truncated).
	 */
	public String makeKey(double d) {
		int a = (int)d;
		int b = Math.abs((int)((d*100)%100));
		StringBuilder buf = new StringBuilder();
		buf.append(a);
		buf.append(".");
		buf.append(b);
		return buf.toString();
	}
}
