package test.java;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xrtb.bidder.AbortableCountDownLatch;
import com.xrtb.bidder.CampaignProcessor;
import com.xrtb.bidder.SelectedCreative;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.geo.GeoTag;
import com.xrtb.geo.Solution;
import com.xrtb.pojo.BidRequest;

import junit.framework.TestCase;

/**
 * A class to test the Geo tagged node subclass works properly.
 * @author Ben M. Faul
 *
 */
public class TestGeoNode  {

	@BeforeClass
	public static void setup() {
		System.out.println("******************  TestGeoNode");
	}
	/**
	 * Test the solution doesn't return null
	 * @throws Exception on file errors
	 */
	@Test
	public void testSolution() throws Exception {
		GeoTag z = new GeoTag();
		z.initTags("data/zip_codes_states.csv",
					"data/unique_geo_zipcodes.txt");
		Solution s = z.getSolution(42.378,71.227);
		assertNotNull(s);
		//System.out.println(s.toString());
	}
}
