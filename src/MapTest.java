import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;


public class MapTest {

	static Gson gson = new Gson();
	
	public static void main(String args[]) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		Map m  = new HashMap();
		String s = "{ \"test\": 1}";
		m = (Map)gson.fromJson(s, Map.class);
		System.out.println("Item = " + m.get("test"));
		
		JsonNode rootNode = mapper.readTree(s);
		System.out.println(rootNode.toString());
	}
}
