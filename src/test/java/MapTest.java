import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import org.junit.Test;


public class MapTest {

	static Gson gson = new Gson();

	@Test
	public void testMapper() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		Map m  = new HashMap();
		String s = "{ \"test\": 1}";
		m = (Map)gson.fromJson(s, Map.class);
		System.out.println("Item = " + m.get("test"));
		
		JsonNode rootNode = mapper.readTree(s);
		System.out.println(rootNode.toString());
		
		List<Integer> list = new ArrayList();
		list.add(new Integer(4));
		Integer x = new Integer(4);
		System.out.println("Member is: " + list.contains(x));
	}
}
