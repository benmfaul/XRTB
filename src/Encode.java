import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

public class Encode {

	public static void main(String args[]) throws Exception {
		File f = new File("www/images");

		List list = new ArrayList();
		File[] files = f.listFiles();
		int n  = 1;
		for (File file : files) {
			String name = (file.getName());
			if (name.length() > 25) {
				String [] parts = name.split("_");
				System.out.println(parts[1]);
				parts = parts[1].split("\\.");
				parts = parts[0].split("x");
				
				System.out.println(parts[0] + ", " + parts[1]);
				
				Map m = new HashMap();
				m.put("forwardurl","http://ms4.ifly.mobi/go/to/clicktrack/click.php?c=106&key=b8f9qfa2up45qh1usc9ld4f6");
				m.put("imageurl", "http://localhost:8080/images/" + name + "?adid={ad_id}&bidid={bid_id}");
				m.put("impid",n++);
				m.put("w", Integer.parseInt(parts[0]));
				m.put("h", Integer.parseInt(parts[1]));
				m.put("price", 0.17);
				m.put("attributes", new ArrayList());
				
				list.add(m);
			}
		}
		
		Gson gson = new Gson();
		System.out.println(gson.toJson(list));
	}
}
