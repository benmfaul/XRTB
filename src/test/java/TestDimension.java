package test.java;

import static org.junit.Assert.*;

import org.junit.Test;

import com.xrtb.common.Dimension;
import com.xrtb.common.Dimensions;

public class TestDimension {

	@Test
	public void testSingle() {
		
		Dimension t = new Dimension(100,100,200,200);
		
		assertTrue(t.fits(null, null));
		assertTrue(t.fits(100, null));
		assertTrue(t.fits(null, 200));
		
		assertTrue(t.fits(100, 200));
		assertFalse(t.fits(99,200));
		assertFalse(t.fits(101,200));
		assertFalse(t.fits(100,199));
		assertFalse(t.fits(100,201));
		
		t = new Dimension(100,300,200,400);
		assertTrue(t.fits(100,200));
		assertTrue(t.fits(300,400));
		assertFalse(t.fits(99,200));
		assertFalse(t.fits(99,400));
		
		assertFalse(t.fits(100,401));	
	}
	
	@Test 
	public void testSmallMediumLarge() {
		Dimension small = new Dimension(0,300,0,400);
		Dimension medium = new Dimension(301,500,401,800);
		Dimension large = new Dimension(501,1000,801,2000);
		
		Dimensions dims = new Dimensions();
		dims.add(small);
		dims.add(medium);
		dims.add(large);
		
		assertTrue(dims.getBestFit(100,200)==small);
		assertTrue(dims.getBestFit(320,500)==medium);
		assertTrue(dims.getBestFit(600,900)==large);
		
		assertNull(dims.getBestFit(400,100));
		
		assertNotNull(dims.getBestFit(null,null));
	}
}
