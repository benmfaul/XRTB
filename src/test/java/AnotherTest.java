import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xrtb.common.URIEncoder;

import org.junit.Test;


// http://www.iab.net/media/file/OpenRTBAPISpecificationVersion2_2.pdf

// http://www.iab.net/guidelines/508676/digitalvideo/vast/vast_xml_samples

// http://support.brightcove.com/en/video-cloud/docs/vast-20-ad-formats-and-format-codes

// http://flash.dev72.flowplayer.org/plugins/advertising/ova.html


public class AnotherTest {
//	static String x = "%3C!DOCTYPE%20html%20PUBLIC%20%5C%22-%2F%2FW3C%2F%2FDTD%20XHTML%201.0%20Transitional%2F%2FEN%5C%22%20%5C%22http%3A%2F%2Fwww.w3.org%2FTR%2Fxhtml1%2FDTD%2Fxhtml1-transitional.dtd%5C%22%3E%3Chtml%20xmlns%3D%5C%22http%3A%2F%2Fwww.w3.org%2F1999%2Fxhtml%5C%22%20xml%3Alang%3D%5C%22en%5C%22%20lang%3D%5C%22en%5C%22%3E...%3C%2Fhtml%3E";
	
	static String x = "%3C%3Fxml%20version%3D%221.0%22%20encoding%3D%22utf-" +
			"8%22%3F%3E%0A%3CVAST%20version%3D%222.0%22%3E%0A%20%20%20%20%3CAd%20id%" +
			"3D%2212345%22%3E%0A%20%20%20%20%20%20%20%20%3CInLine%3E%0A%20%20%20%20" +
			"%20%20%20%20%20%20%20%20%3CAdSystem%20version%3D%221.0%22%3ESpotXchange%3" +
			"C%2FAdSystem%3E%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%3C" +
			"AdTitle%3E%3C!%5BCDATA%5BSample%20VAST%5D%5D%3E%3C%2FAdTitle%3E%0A%20%20%" +
			"20%20%20%20%20%20%20%20%20%20%20%20%20%20%3CImpression%3Ehttp%3A%2F%2Fsa" +
			"mple.com%3C%2FImpression%3E%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%2" +
			"0%20%20%3CDescription%3E%3C!%5BCDATA%5BA%20sample%20VAST%20feed%5D%5D%3E%" +
			"3C%2FDescription%3E%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%" +
			"3CCreatives%3E%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%2" +
			"0%20%20%3CCreative%20sequence%3D%221%22%20id%3D%221%22%3E%0A%20%20%20%20" +
			"%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%3CLinear" +
			"%3E%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%" +
			"20%20%20%20%20%20%20%20%3CDuration%3E00%3A00%3A30%3C%2FDuration%3E%0A%20" +
			"%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%2" +
			"0%20%20%20%20%3CTrackingEventCreative%20%20%20%20%20%20%20%20%20%20%2" +
			"0%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%3C%2FTrackingEvents%3" +
			"E%20%20%20%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20" +
			"%20%20%20%20%20%20%20%20%20%20%3CVideoClicks%3E%0A%20%20%20%20%20%20%20" +
			"%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%2" +
			"0%20%20%3CClickThrough%3E%3C!%5BCDATA%5Bhttp%3A%2F%2Fsample.com%2Fopenrtbtes" +
			"t%5D%5D%3E%3C%2FClickThrough%3E%0A%20%20%20%20%20%20%20%20%20%20%20%20" +
			"%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%3C%2FVideoClicks%3E%0A" +
			"%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%2" +
			"0%20%20%20%20%20%3CMediaFiles%3E%0A%20%20%20%20%20%20%20%20%20%20%20%2" +
			"0%20%20%20%20%20%20%20Creative%20%20%20%20%20%20%20%20%20%3CMedi" +
			"aFile%20delivery%3D%22progressive%22%20bitrate%3D%22256%22%20width%3D%22640%22" +
			"%20height%3D%22480%22%20type%3D%22video%2Fmp4%22%3E%3C!%5BCDATA%5Bhttp%3" +
			"A%2F%2Fsample.com%2Fvideo.mp4%5D%5D%3E%3C%2FMediaFile%3E%0A%20%20%20%20%" +
			"20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20" +
			"%20%Creativeiles%3E%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20" +
			"OPENRTB API Specification Version 2.2 RTB Project" +
			"Page 62" +
			"%20%20%20%20%20%20%20%20%20%3C%2FLinear%3E%0A%20%20%20%20%20%20%20%20" +
			"%20%20%20%20%20%20%20%20%20%20%20%20%3C%2FCreative%3E%0A%20%20%20%20%2" +
			"0%20%20%20%20%20%20%20%20%20%20%20%3C%2FCreatives%3E%0A%20%20%20%20%20" +
			"%20%20%20%3C%2FInLine%3E%0A%20%20%20%20%3C%2FAd%3E%0A%3C%2FVAST%3E"; 
	
	//public static String x= "%3C%3Fxml%20version%3D%221.0%22%20encoding%3D%22utf-";
	@Test
	public static void testMain() throws Exception {
		
	/*	StringBuilder y = null;
		long time = System.currentTimeMillis();
		for (int i=0; i < 1000000; i++) {
			y = new StringBuilder(x);
			replaceAll(y,"Creative","Fuck");
		}
		time = System.currentTimeMillis() - time;
		System.out.println("TIME = " + time/1000);
		
		time = System.currentTimeMillis();
		for (int i=0; i < 1000000; i++) {
			y = new StringBuilder(x);
			replaceAll(y,"Creative","Fuck");
		}
		time = System.currentTimeMillis() - time;
		System.out.println("TIME = " + time/1000);
		*/
		
		StringBuilder y = new StringBuilder("I am a string with&a=1 and &b = 2");
		xmlEscape(y);
	}
	
	public static void xmlEscape(StringBuilder sb) {
		int i = 0;
		while(i < sb.length()) {
			i = sb.indexOf("&",i);
			System.out.println(i);
			if (i == -1)
				return;
			if (!(sb.charAt(i+1)=='a' &&
					sb.charAt(i+2)=='m' &&
					sb.charAt(i+3)=='p' &&
					sb.charAt(i+4)==';')) {				
					
				sb.insert(i+1,"amp;");		
			}
			i += 4;
		}
	}
	
	public static void replaceAll(StringBuilder builder, String from, String to)
	{
	    int index = builder.lastIndexOf(from);
	    while (index != -1)
	    {
	        builder.replace(index, index + from.length(), to);
	        index += to.length(); // Move to the end of the replacement
	        index = builder.lastIndexOf(from, index);
	    }
	}
	
	/**
	 * Replace a single instance of string.
	 * @param x StringBuilder. The buffer to do replacements in.
	 * @param what String. The string we are looking to replace.
	 * @param sub String. The string to use for the replacement.
	 * @return the same string buffer passed as the first param.
	 */
	public static void replace(StringBuilder x, String what, String sub) {
		if (what == null || sub == null)
			return;
	
		int start = x.indexOf(what);
		if (start != -1) {
			x.replace(start, start+what.length(), sub);
			replace(x,what,sub);
		}
	}
}
