import com.fasterxml.jackson.core.io.JsonStringEncoder;

public class TestEncoding {

	public static void main(String [] args) {
		 JsonStringEncoder encoder = JsonStringEncoder.getInstance();
		 String test = "This is \\\\\" a test";
		char[] output =  encoder.quoteAsString(test);
		String end = new String(output);
		System.out.println(test);;
		System.out.println(end);;
	}
}
