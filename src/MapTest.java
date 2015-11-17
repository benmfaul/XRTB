import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

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
