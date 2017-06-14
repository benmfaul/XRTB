import java.util.HashMap;
import java.util.Map;

public class TestInstanceOf {

	public static void main(String args[]) throws Exception {
		
		Map x = new HashMap();
		System.out.println(x.getClass().getSimpleName());
		
		int k = 0;
		long y = System.currentTimeMillis();
		for (int i=0;i<1000000;i++) {
			if (x instanceof HashMap) {
				k++;
			}
		}
		y = System.currentTimeMillis() - y;
		System.out.println(y);;
		
		y = System.currentTimeMillis();
		for (int i=0;i<1000000;i++) {
			k=1;
			switch(k) {
			case 0:
				break;
			case 1:
				break;
			case 2:
				break;
			case 3:
				k++;
				break;
			default:
				
			}
		}
		y = System.currentTimeMillis() - y;
		System.out.println(y);;
		
		y = System.currentTimeMillis();
		for (int i=0;i<1000000;i++) {
			if (x.getClass().getSimpleName().equals("HashMap"))
				k++;
		}
		y = System.currentTimeMillis() - y;
		System.out.println(y);;
		
	}
}
