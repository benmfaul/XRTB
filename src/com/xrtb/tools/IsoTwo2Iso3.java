package com.xrtb.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import com.xrtb.blocks.LookingGlass;

/**
 * A class to create a map to look up 2 char ISO country codes and return 3 char country codes.
 * @author Ben M. Faul
 *
 */
public class IsoTwo2Iso3 extends LookingGlass {

	Map<String, String> iso = new HashMap<String, String>();
	
	public static void main(String [] args) throws Exception {
		IsoTwo2Iso3 x = new IsoTwo2Iso3("@ISO2-3", "data/adxgeo.csv");
		String y = x.query("GB");
		System.out.println("I got: " + y);;
	}
	
	/**
	 * Constructor for the symbol table entry
	 * @param name String. The name of the map in the symbol table.
	 * @param file String. The name of the file to load
	 * @throws Exception on file I/O errors.
	 */
	public IsoTwo2Iso3(String name, String file) throws Exception {
		super();
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		String[] parts;
		for (String line; (line = br.readLine()) != null;) {
			parts = eatquotedStrings(line);
			InternalIso x = new InternalIso(parts);
			iso.put(x.iso2, x.iso3);
		}
		symbols.put(name, this);
		br.close();
	}
	
	/**
	 * Query the map
	 * @param code String. The 2 char ISO country code.
	 * @return String. The 3 char ISO country code.
	 */
	public String query(String code) {
		if (code.length() == 3) return code;
		return iso.get(code);
	}
}


/**
 * Internal representation of the CSV file of the input data.
 * @author Ben M. Faul
 *
 */
class InternalIso {
	public Integer code;
	public String name;
	public String country_and_name;
	public String isocode;
	public String iso2;
	public String type;
	public String mode;
	public String iso3;
	
	/**
	 * Create the internal object from the tokens.
	 * @param tokens String[]. An array of string tokens.
	 */
	public InternalIso(String[] tokens) {
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
