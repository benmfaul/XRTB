package test.java;

import com.xrtb.RedissonClient;
import com.xrtb.common.Configuration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TestFreqCapLogc {

	private static ZoneId DEFAULT_ZONE = ZoneId.of("UTC");
	private LocalDateTime REFERENCE_DATE_TIME = LocalDateTime.of(2017, 12, 10, 10, 10, 15);
	private List<String> frequencyCapTimeUnit = Arrays.asList("minutes", "hours", "days", "lifetime");

	private static RedissonClient redisson;

	@BeforeClass
	public static void setup() {
		try {
			Config.setup();
			redisson = Configuration.getInstance().redisson;
			System.out.println("******************  TestFreqCapLogic");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Shut the RTB server down.
	 */
	@AfterClass
	public static void testCleanup() {
		Config.teardown();
		System.out.println("We are done!");
	}


	@Test
	public void testExpire() throws Exception {

		redisson.del("JUNK");
		redisson.set("JUNK", "1000",5);
		Thread.sleep(4000);
		assertTrue(redisson.get("JUNK").equals("1000"));
		Thread.sleep(2000);
		assertNull(redisson.get("JUNK"));

	}

	@Test
	public void testGetTimeToLiveInSecondsRoundedToNearestTimeUnitBaseOnUtcClock() throws Exception {
		RedissonClient.clock = Clock.fixed(REFERENCE_DATE_TIME.atZone(DEFAULT_ZONE).toInstant(), DEFAULT_ZONE);

		//Test for minutes
		long ttl = RedissonClient.getTimeToLiveInSecondsRoundedToNearestTimeUnitBaseOnUtcClock(60 * 1, frequencyCapTimeUnit.get(0)); // expire in 1 min
		assertEquals("Wrong ttl for minutes time unit", 45, ttl);
		ttl = RedissonClient.getTimeToLiveInSecondsRoundedToNearestTimeUnitBaseOnUtcClock(60 * 2, frequencyCapTimeUnit.get(0)); // expire in 2 mins
		assertEquals("Wrong ttl for minutes time unit", 105, ttl);

		//Test for hours
		ttl = RedissonClient.getTimeToLiveInSecondsRoundedToNearestTimeUnitBaseOnUtcClock(60 * 60 * 1, frequencyCapTimeUnit.get(1)); // expire in 1 hour
		assertEquals("Wrong ttl for hours time unit", 2985, ttl);
		ttl = RedissonClient.getTimeToLiveInSecondsRoundedToNearestTimeUnitBaseOnUtcClock(60 * 60 * 2, frequencyCapTimeUnit.get(1)); // expire in 2 hours
		assertEquals("Wrong ttl for hours time unit", 6585, ttl);

		//Test for days
		ttl = RedissonClient.getTimeToLiveInSecondsRoundedToNearestTimeUnitBaseOnUtcClock(60 * 60 * 24 * 1, frequencyCapTimeUnit.get(2)); // expire in 1 day
		assertEquals("Wrong ttl for days time unit", 49785, ttl);
		ttl = RedissonClient.getTimeToLiveInSecondsRoundedToNearestTimeUnitBaseOnUtcClock(60 * 60 * 24 * 2, frequencyCapTimeUnit.get(2)); // expire in 2 days
		assertEquals("Wrong ttl for days time unit", 136185, ttl);
	}

}
