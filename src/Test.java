import java.io.IOException;

/**
 * String indexOf is faster than StringBuffer
 * Replace, StringBuffer much faster, at leat 10x
 * @author ben
 *
 */
public class Test {

	public static void main(String []args) throws Exception {

		StringBuilder a = new StringBuilder("                                   2210-91-0293-0193-012");
		long start = System.currentTimeMillis();
		for (int i=0;i<1000000;i++) {
			a.replace(0, a.length(), "1039-0192-0492-3049-0239-23094-02394-02");
		}
		long stop = System.currentTimeMillis() - start;
		
		StringBuffer b = new StringBuffer("                                   2210-91-0293-0193-012");
		start = System.currentTimeMillis();
		for (int i=0;i<1000000;i++) {
			b.replace(0, b.length(), "1039-0192-0492-3049-0239-23094-02394-02");
		}
		long stop1 = System.currentTimeMillis() - start;
		
		System.out.println("A="+stop+", B="+stop1);
	}
}
