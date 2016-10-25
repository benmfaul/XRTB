import java.io.PrintWriter;
import java.io.StringWriter;

public class Junk {

	public static void main(String[] args) throws Exception {
		new Junk();
	}
	
	
	public Junk() throws Exception {
		try { 
			one();
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String str = sw.toString();
			String lines [] = str.split("\n");
			System.out.println(lines[0] + ", " + lines[1]);
		}
	}
	
	public void one() throws Exception {
		two();
	}
	
	public void two()throws Exception {
			
		System.out.println(1/0);
	}
}
