package com.xrtb.geo;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeoTag {

	public final double ERADIUS = 6371;
	public final double PI = 3.14159;
	Map<String, List<Solution>> table = new HashMap();
	Map<Integer, List<String>> zipStates = new HashMap();

	public GeoTag() {
		
	}
	public void initTags(String zipState, String datafile) throws Exception {
		loadZipMap(zipState);
		loadDatabase(datafile);
	}

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

	public Solution getSolution(double lat, double lon) {
		double d = 0;
		String key = makeKey(lat);
		//key = "33.75";
		List<Solution> ps = table.get(key);
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

	public void getZipValues(List<String> vals, String data) {

	}

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
		 double distance=earth*cHarv;
		 return distance;
	}

	public String makeKey(double d) {
		int a = (int)d;
		int b = Math.abs((int)((d*100)%100));
		StringBuffer buf = new StringBuffer();
		buf.append(a);
		buf.append(".");
		buf.append(b);
		return buf.toString();
	}
}
