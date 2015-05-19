import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.print.attribute.standard.MediaSize.Engineering;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.xrtb.bidder.RTBServer;


public class NashHorn {
	private ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
	String html;
	
	public static void main(String[] args) throws Exception {
		NashHorn horn = new NashHorn("index.html");
		System.out.println(horn.getHtml());
	}
	
	public NashHorn(String fileName) throws Exception {
	    String str = Charset
					.defaultCharset()
					.decode(ByteBuffer.wrap(Files.readAllBytes(Paths
							.get(fileName)))).toString();
		StringBuffer data = new StringBuffer(str);
		while(getTagValues(data));
		html = data.toString();
	}
	
	private boolean getTagValues(StringBuffer buf) throws Exception {
		int start= buf.indexOf("<%");
		int stop = buf.indexOf("%>");
		if (stop < 0 || stop < 0)
			return false;
		
	    String code = buf.substring(start+2,stop);
	    Object o = engine.eval(code);
	    buf.replace(start-2, stop+2, o.toString());
	    return true;
	}
	
	public String getHtml() {
		return html;
	}

}
