package com.xrtb.tests;

import junit.framework.TestCase;

import org.junit.Test;

import com.xrtb.geo.GeoTag;
import com.xrtb.geo.Solution;

public class TestGeoTag extends TestCase {

	@Test
	public void testHaversignCalc()  {
		try {
		GeoTag z = new GeoTag();
		z.initTags("data/zip_codes_states.csv",
					"data/unique_geo_zipcodes.txt");
		Solution p = null;
		long time = System.currentTimeMillis();
		p = z.getSolution(33.7550,-84.39);
		assertTrue(p.code==30303);
		assertTrue(p.state.equals("GA"));
		assertTrue(p.county.equals("Fulton"));
		} catch (Exception error) {
			error.printStackTrace();
			fail(error.toString());
		}
	}
}
