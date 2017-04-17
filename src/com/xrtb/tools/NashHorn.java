package com.xrtb.tools;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * A class for executing server side javascript.
 * Copyright c, 2015 Eeminder Inc., all rights reserved.
 * @author Ben M. Faul
 *
 */
public class NashHorn {
	/** The scripting engine we will use */
	private ScriptEngine engine = new ScriptEngineManager()
			.getEngineByName("nashorn");
	String staticJS;
	
	StringBuilder data = new StringBuilder();


	/**
	 * Execute the javascript contained in the file.
	 * @param f File. The file containing the js code. 
	 * @throws Exception on IO and Javascript language errors.
	 */
	public NashHorn(File f) throws Exception {
		String str = Charset
				.defaultCharset()
				.decode(ByteBuffer.wrap(Files.readAllBytes(Paths.get(f
						.getPath())))).toString();
		data = new StringBuilder(str);
		engine.put("nash", this);
	}
	
	public NashHorn()  {
		engine.put("nash", this);
	}

	/**
	 * Execute a string containing javascript.
	 * @param str String. The javascript to execute.
	 * @throws Exception on JavaScript langiage errors.
	 */
	public NashHorn(String str)  {
		data = new StringBuilder(str);
		engine.put("nash", this);
	}

	/**
	 * Add a JavaScript object in the JS variable name space.
	 * @param key String. The name of tha variable.
	 * @param o Object. The object of that variable.
	 */
	public void setObject(String key, Object o) {
		engine.put(key, o);
	}
	
	public void execute(String script) throws Exception {
		engine.eval(script);
	}

	/**
	 * Pluck the code between <% and %>, execute it, then take the result and replace the old text.
	 * @param buf StringBuffer. The html page.
	 * @return boolean. True if a tag was found, otherwise false.
	 * @throws Exception on JavaScript language errors or unbalanced <% %> tags.
	 */
	private boolean getTagValues(StringBuilder buf) throws Exception {
		int start = buf.indexOf("<%");
		int stop = buf.indexOf("%>");
		if (stop < 0 || stop < 0)
			return false;

		String code = buf.substring(start + 2, stop);
		Object o = engine.eval(code);
		if (o == null)
			o = "";
		else if (o instanceof String)
			o = o.toString();
		else
			o = "";

		buf.replace(start, stop + 2, o.toString());
		if (start != 0 && buf.length() < start + 1) {
			if (buf.charAt(start) == '\n' && buf.charAt(start - 1) == '\n')
				buf.replace(start - 1, start + 1, "\n");
		}

		return true;
	}

	
	/**
	 * Return the HTML page. This does the actual conversion.
	 * @return String. The computed HTML.
	 * @throws Exception on JavaScript language errors.
	 */
	public String getHtml() throws Exception {
		String html = null;
		while (getTagValues(data));
		html = data.toString();
		
		return html;
	}
}
