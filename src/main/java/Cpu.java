import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;


public class Cpu {
	public static void main(String args[]) throws Exception {
	//	for (int i=0;i<10;i++)
	//	new Looper();
	//	Thread.sleep(70000);
		System.out.println("Load: " + getProcessCpuLoad());
	}
	public static double getProcessCpuLoad() throws Exception {

	    MBeanServer mbs    = ManagementFactory.getPlatformMBeanServer();
	    ObjectName name    = ObjectName.getInstance("java.lang:type=OperatingSystem");
	    AttributeList list = mbs.getAttributes(name, new String[]{ "ProcessCpuLoad" });

	    if (list.isEmpty())     return Double.NaN;

	    Attribute att = (Attribute)list.get(0);
	    Double value  = (Double)att.getValue();

	    // usually takes a couple of seconds before we get real values
	    if (value == -1.0)      return Double.NaN;
	    System.out.println(value);
	    // returns a percentage value with 1 decimal point precision
	    
	    
	    
	    DecimalFormat formatter = new DecimalFormat("###.###", DecimalFormatSymbols.getInstance( Locale.ENGLISH ));
	    formatter.setRoundingMode( RoundingMode.DOWN );
	    OperatingSystemMXBean mx =
	    	    java.lang.management.ManagementFactory.getOperatingSystemMXBean();
	    int cores = Runtime.getRuntime().availableProcessors();
	    double d = mx.getSystemLoadAverage() * 100 / cores;
	    String s = formatter.format(d);
	    
	    System.out.println("===>"  + s + ", cores=" + cores);
	    
	    
	    return ((int)(value * 1000) / 10.0);
	}
}

class Looper implements Runnable {
	Thread me;
	
	public Looper() {
	
		me = new Thread(this);
		me.start();
	}
	
	public void run() {
		while(true) {
			int i = 1;
			i = i + 1;
			int j;
			j = i;
		}
	}
}