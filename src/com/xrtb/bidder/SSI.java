package com.xrtb.bidder;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * A class that handles server side includes for the RTBServer web server.
 * @author Ben M. Faul
 *
 */

public class SSI {
	
	private static final String PREAMBLE = "<!--#include virtual=\"";
	public static String convert(String page) throws Exception {

		if (page.contains(PREAMBLE)) { // don't allocate resources when it is not needed.
			StringBuilder sb = new StringBuilder(page);
			while(getTagValues(sb));

			page = sb.toString();
		}

		return page;
	}
	
	/**
	 * Pluck the code between <% and %>, execute it, then take the result and replace the old text.
	 * @param buf StringBuffer. The html page.
	 * @return boolean. True if a tag was found, otherwise false.
	 */
	private static boolean getTagValues(StringBuilder buf) {
		int start = buf.indexOf(PREAMBLE);
		int stop = buf.indexOf("-->",start);
		if (start < 0 || stop < 0)
			return false;

		String code = new String(buf.substring(start + PREAMBLE.length(), stop));
		
		int tail = code.indexOf("\"");
		code = code.substring(0, tail);
		if (!code.startsWith("www")) {
			code = "www/" + code;
		}
		
		String inner  = null;
		try {
			
			inner = Charset
				.defaultCharset()
				.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
						.get(code)))).toString();

			buf.replace(start, stop + 3, inner);
		} catch(Exception error) {
			error.printStackTrace();
			return false;
		}
		if (start != 0 && buf.length() < start + 1) {
			if (buf.charAt(start) == '\n' && buf.charAt(start - 1) == '\n')
				buf.replace(start - 1, start + 1, "\n");
		}

		return true;
	}

}
