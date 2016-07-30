
public class TestFormat {

	private static final int POW10[] = {1, 10, 100, 1000, 10000, 100000, 1000000};

	public static void main(String [] args) {
		
		double x  = .1234;
		long ta = 0;
		long tb = 0;
		long tc = 0;
		
		String a = "";
		
		long time = 0;
		
		for (int i=0;i<1000000;i++) {
			time = System.currentTimeMillis();
			a = "" + x;
			ta += System.currentTimeMillis() - time;
		}
		System.out.println(a);
		
		for (int i=0;i<1000000;i++) {
			time = System.currentTimeMillis();
			a = Double.toString(x);
			tb += System.currentTimeMillis() - time;
		}
		System.out.println(a);
		
		for (int i=0;i<1000000;i++) {
			time = System.currentTimeMillis();
			a = format(x,6);
			tc += System.currentTimeMillis() - time;
		}
		System.out.println(a);
		
		double z = (double)tb/(double)ta;
		System.out.println(z);
		z = (double)tc/(double)ta;
		System.out.println(z);

	}
	
	public static String format(double val, int precision) {
	     StringBuilder sb = new StringBuilder();
	     if (val < 0) {
	         sb.append('-');
	         val = -val;
	     }
	     int exp = POW10[precision];
	     long lval = (long)(val * exp + 0.5);
	     sb.append(lval / exp).append('.');
	     long fval = lval % exp;
	     for (int p = precision - 1; p > 0 && fval < POW10[p]; p--) {
	         sb.append('0');
	     }
	     sb.append(fval);
	     return sb.toString();
	 }
}
