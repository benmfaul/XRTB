package com.xrtb.exchanges.adx;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import com.xrtb.tools.LookingGlass;

public class AdxGeoCodes extends LookingGlass {

	Map<Integer, AdxGeoCode> geocodes = new HashMap();
	
	public AdxGeoCodes(String name, String file) throws Exception {
		super();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String message = null;

		String[] parts = null;

		message = "Initialize Adx Geocodes: " + file + " as " + name;
		for (String line; (line = br.readLine()) != null;) {
			parts = eatquotedStrings(line);
			AdxGeoCode x = new AdxGeoCode(parts);
			geocodes.put(x.code, x);
		}
		symbols.put(name, this);
	}
	
	public AdxGeoCode query(Integer code) {
		return geocodes.get(code);
	}
}


class AdxGeoCode {
	public Integer code;
	public String name;
	public String country_and_name;
	public String isocode;
	public String iso2;
	public String type;
	public String mode;
	public String iso3;
	
	public AdxGeoCode(String[] tokens) {
		for (int i = 0; i<tokens.length;i++) {
			tokens[i] = tokens[i].trim();
		}
		code = Integer.parseInt(tokens[0]);
		name = tokens[1];
		country_and_name = tokens[2];
		isocode = tokens[3];
		iso2 = tokens[4];
		type = tokens[5];
		mode = tokens[6];
		iso3 = tokens[7];
	}
}
