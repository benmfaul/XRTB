package com.xrtb.tests;

import java.util.Arrays;

import java.util.List;

import org.junit.Test;

import com.xrtb.common.Configuration;
import com.xrtb.geo.GeoNode;
import com.xrtb.geo.GeoTag;
import com.xrtb.pojo.BidRequest;

import junit.framework.TestCase;

/**
 * A class to test the Geo tagged node subclass works properly.
 * @author Ben M. Faul
 *
 */
public class TestGeoNode extends TestCase {

	@Test
	public void testCambridgeCity() throws Exception  {
		GeoTag z = new GeoTag();
		z.initTags("data/zip_codes_states.csv",
					"data/unique_geo_zipcodes.txt");
		GeoNode.tag = z;
		BidRequest br = new BidRequest(Configuration.getInputStream("SampleBids/nexage.txt"));
		assertNotNull(br);
		List<String> list = Arrays.asList("NY", "MA", "CA");
		GeoNode node = new GeoNode("geo-test","STATE","MEMBER",list);
		boolean b = node.test(br);	   // true means the constraint is satisfied.
		assertTrue(b);
	}
}
