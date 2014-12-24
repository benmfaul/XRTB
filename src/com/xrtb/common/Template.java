package com.xrtb.common;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;

/**
 * A class for defining multiple ADM fields on a per Exchange basis
 * @author Ben M. Faul
 *
 */
public class Template {

	/** The default ADM used if no exchange template exists */
	String defaultAdm;
	
	/** Map of templates with exchahnges being the key */
	Map<String,String> exchange;
	
	public static final String RTB_REDIRECT_URL = "RTB_REDIRECT_URL";
	public static final String RTB_CAMPAIGN_ADID = "RTB_CAMPAIGN_ADID";
	public static final String RTB_PIXEL_URL = "RTB_PIXEL_URL";
	
	/** The builtin macro substtitutions */
	public static final List<String> builtins = new ArrayList();
	static {
		builtins.add("pub");	
		builtins.add("bid_id");
		builtins.add("ad_id");
		builtins.add("campaign_forward_url");
		builtins.add("creative_id");
		builtins.add("campaign_image_url");
		builtins.add("campaign_ad_height");
		builtins.add("campaign_ad_width");
	}
	
	/**
	 * Empty constructor for use with JSON
	 */
	public Template() {
		
	}
	/**
	 * A method for more efficient replaceAll() in a string. This is 3 - 7x faster that String.replaceAll()
	 * @param str String. The string to be operated on, the source.
	 * @param searchChars String. The string you are looking to replace
	 * @param replaceChars String. The string you want to substitute.
	 * @return String. A new String with the replacements (if any).
	 */
	
	public static String replaceAll(final String str, final String searchChars, String replaceChars)
	{
	  if ("".equals(str) || "".equals(searchChars) || searchChars.equals(replaceChars))
	  {
	    return str;
	  }
	  if (replaceChars == null)
	  {
	    replaceChars = "";
	  }
	  final int strLength = str.length();
	  final int searchCharsLength = searchChars.length();
	  StringBuilder buf = new StringBuilder(str);
	  boolean modified = false;
	  for (int i = 0; i < strLength; i++)
	  {
	    int start = buf.indexOf(searchChars, i);

	    if (start == -1)
	    {
	      if (i == 0)
	      {
	        return str;
	      }
	      return buf.toString();
	    }
	    buf = buf.replace(start, start + searchCharsLength, replaceChars);
	    modified = true;

	  }
	  if (!modified)
	  {
	    return str;
	  }
	  else
	  {
	    return buf.toString();
	  }
	}
}
