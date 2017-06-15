package com.xrtb.exchanges.adx;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.xrtb.blocks.LookingGlass;

public class MakeMasterGeoTable {

	static Map<String, String> map = new HashMap();
	
	public static void main(String [] args) throws Exception  {
		BufferedReader br = new BufferedReader(new FileReader("/home/ben/Downloads/countrycodes.csv"));
		String message = null;

		String[] parts = null;
		
		for (String line; (line = br.readLine()) != null;) {
			parts = LookingGlass.eatquotedStrings(line);
			map.put(parts[1], parts[2]);
		}
		br.close();
		
		BufferedWriter writer = Files.newBufferedWriter(Paths.get("data/adxgeo.csv"));

		br = new BufferedReader(new FileReader("/home/ben/Downloads/adx-geo.csv"));
		for (String line; (line = br.readLine()) != null;) {
			parts = LookingGlass.eatquotedStrings(line);
			String iso3 = map.get(parts[4]);
			if (iso3 != null) {
				line += ", " + iso3 +"\n";
				writer.write(line);
				System.out.print(line);
			}
		}
		writer.close();
	}
}
