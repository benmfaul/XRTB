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
		Dimension small = new Dimension(0,300,-1,-1);
		Dimension medium = new Dimension(301,400,-1,-1);
		Dimension large = new Dimension(501,1000,-1,-1);
		
		Dimensions dims = new Dimensions();
		dims.add(small);
		dims.add(medium);
		dims.add(large);
		
		
		assertTrue(dims.getBestFit(100,200)==small);
		
		Dimension x = dims.getBestFit(320,500);
		assertTrue(x==medium);
		assertTrue(dims.getBestFit(600,900)==large);
		
		assertNull(dims.getBestFit(410,500));
		
		assertNotNull(dims.getBestFit(null,null));
	}
}
