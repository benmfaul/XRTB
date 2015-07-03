package com.xrtb.common;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Simple uri encoder, made from the spec at http://www.ietf.org/rfc/rfc2396.txt
 * 
 * @author Ben M. Faul
 */
public class URIEncoder {
	/** HTML marks */
	private static final String mark = "-_.!~*'()\"";
	/** Hex digits */
	private static final char[] hex = "0123456789ABCDEF".toCharArray();

	/**
	 * Given a string, encode it into URI form.
	 * 
	 * @param argString
	 *            String. Unencoded uri.
	 * @return String. The encoded uri.
	 */
	public static String encodeURI(String argString) {
		StringBuilder uri = new StringBuilder();

		char[] chars = argString.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z')
					|| (c >= 'A' && c <= 'Z') || mark.indexOf(c) != -1) {
				uri.append(c);
			} else {
				appendEscaped(uri, c);
			}
		}
		return uri.toString();
	}

	/**
	 * Appends a character to the uri string buffer.
	 * 
	 * @param uri
	 *            StringBuilder. The url we are building.
	 * @param c
	 *            char. The character to append/
	 */
	private static void appendEscaped(StringBuilder uri, char c) {
		if (c <= (char) 0xF) {
			uri.append("%");
			uri.append('0');
			uri.append(hex[c]);
		} else if (c <= (char) 0xFF) {
			uri.append("%");
			uri.append(hex[c >> 8]);
			uri.append(hex[c & 0xF]);
		} else {
			// unicode
			uri.append('\\');
			uri.append('u');
			uri.append(hex[c >> 24]);
			uri.append(hex[(c >> 16) & 0xF]);
			uri.append(hex[(c >> 8) & 0xF]);
			uri.append(hex[c & 0xF]);
		}
	}

	/**
	 * Returns the encoded Uri based on the input.
	 * 
	 * @param s String. The string to encode.
	 * @return String. The encoded string.
	 */
	public static String myUri(String s) {
		String result;
		if (s == null)
			return null;

		try {
			result = URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20")
					.replaceAll("\\%21", "!").replaceAll("\\%27", "'")
					.replaceAll("\\%28", "(").replaceAll("\\%29", ")")
					.replaceAll("\\%7E", "~");
		} catch (Exception e) {
			result = s;
		}

		return result;
	}
}
