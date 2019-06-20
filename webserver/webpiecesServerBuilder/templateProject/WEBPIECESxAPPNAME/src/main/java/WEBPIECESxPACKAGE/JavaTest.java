package WEBPIECESxPACKAGE;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class JavaTest {
	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
	    String json =     "{ \"thing1\": \"value1\", \"thing2\": { \"sub - thing2\": \"sub - value2\", \"sub - thing3\": 291231 }, \"thing3\": [ \"a\", \"b\", \"c\", \"d\" ] } ";
//		String json = "{ \"thing2\": { \"sub - thing2\": \"sub - value2\", \"sub - thing3\": 291231 }, \"thing1\": \"value1\", \"thing3\": [ \"a\", \"b\", \"c\", \"d\" ] } ";

		System.out.println("json=" + json);

		ObjectMapper obj = new ObjectMapper();
		@SuppressWarnings("unchecked")
		Map<String, Object> map = obj.readValue(json, Map.class);

		for (Map.Entry<String, Object> entries : map.entrySet()) {
			System.out.println("key=" + entries.getKey() + " value=" + entries.getValue().getClass());

		}
	}
}
