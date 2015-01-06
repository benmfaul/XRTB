package com.xrtb.common;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * simple uri encoder, made from the spec at http://www.ietf.org/rfc/rfc2396.txt
 */
public class URIEncoder {
  private static final String mark = "-_.!~*'()\"";
  private static final char[] hex = "0123456789ABCDEF".toCharArray();
 
  public static String encodeURI(String argString) {
    StringBuilder uri = new StringBuilder();
 
    char[] chars = argString.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      char c = chars[i];
      if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') ||
          (c >= 'A' && c <= 'Z') || mark.indexOf(c) != -1) {
        uri.append(c);
      } else {
        appendEscaped(uri, c);
      }
    }
    return uri.toString();
  }
 
  private static void appendEscaped(StringBuilder uri, char c) {
    if (c <= (char) 0xF) {
      uri.append("%");
      uri.append('0');
      uri.append(hex[ c]);
    } else if (c <= (char) 0xFF) {
      uri.append("%");
      uri.append(hex[ c >> 8]);
      uri.append(hex[ c & 0xF]);
    } else {
      // unicode
      uri.append('\\');
      uri.append('u');
      uri.append(hex[ c >> 24]);
      uri.append(hex[(c >> 16) & 0xF]);
      uri.append(hex[(c >> 8) & 0xF]);
      uri.append(hex[ c & 0xF]);
    }
  }
  
  
  public static String myUri(String s) {
  String result;

  try {
      result = URLEncoder.encode(s, "UTF-8")
              .replaceAll("\\+", "%20")
              .replaceAll("\\%21", "!")
              .replaceAll("\\%27", "'")
              .replaceAll("\\%28", "(")
              .replaceAll("\\%29", ")")
              .replaceAll("\\%7E", "~");
  } catch (UnsupportedEncodingException e) {
      result = s;
  }

  return result;
  }
}
