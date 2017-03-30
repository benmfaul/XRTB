package com.xrtb.tools;

/**
 * Replacement for dog slow Java Random.
 * @author Ben M. Faul
 *
 */
public class XORShiftRandom {

	// The start. 
	private static long  state = 0xCAFEBABE; // initial non-zero value
	
	/**
	 * Constructor
	 */
	public XORShiftRandom() {
		
	}

	/**
	 * Return the next random long value
	 * @return long. The next random number in the sequence.
	 */
	public long nextLong() {
	  long a=state;
	  state = xorShift64(a);
	  return a;
	}

	/**
	 * Xor shift
	 * @param a long. The blue to xorshift.
	 * @return long. The xorshifted value
	 */
	public  long xorShift64(long a) {
	  a ^= (a << 21);
	  a ^= (a >>> 35);
	  a ^= (a << 4);
	  return a;
	}

	/**
	 * Return the next random int, from 0 to n-1.
	 * @param n int. The upper bound.
	 * @return int. The next random integer.
	 */
	public int random(int n) {
	  if (n<0) throw new IllegalArgumentException();
	  long result=((nextLong()>>>32)*n)>>32;
	  return (int) result;
	}
}
