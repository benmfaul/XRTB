package com.xrtb.tests;

import static org.junit.Assert.*;
import junit.framework.TestCase;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xrtb.geo.GeoTag;
import com.xrtb.geo.Solution;

/**
 * Make sure GPS to zip code conversion works as expected.
 * @author Ben M. Faul
 *
 */
public class TestGeoTag  {

	@BeforeClass
	public static void setup() {
		try {
			Config.setup();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void stop() {
		Config.teardown();
	}
	
	@Test
	public void testHaversignCalc()  {
		try {
		GeoTag z = new GeoTag();
		z.initTags("data/zip_codes_states.csv",
					"data/unique_geo_zipcodes.txt");
		Solution p = null;
		long time = System.currentTimeMillis();
		p = z.getSolution(42.378,-71.227);
		assertTrue(p.code==2238);
		assertTrue(p.state.equals("MA"));
		assertTrue(p.county.equals("Middlesex"));
		} catch (Exception error) {
			error.printStackTrace();
			fail(error.toString());
		}
	}
}
