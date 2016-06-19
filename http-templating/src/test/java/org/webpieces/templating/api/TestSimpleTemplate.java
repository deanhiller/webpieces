package org.webpieces.templating.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileImpl;

public class TestSimpleTemplate {

	@Test
	public void testBasic() throws IOException {
		TemplateEngine engine = TemplatingFactory.create();
		
		UserBean userBean = new UserBean();
		userBean.setName("Dean");
		userBean.setNumSiblings(2);
		Map<String, Object> arguments = new HashMap<>();
		arguments.put("name", "Dean");
		arguments.put("color", "blue");
		arguments.put("user", userBean);
		
		ClassLoader cl = TestSimpleTemplate.class.getClassLoader();
		URL resource = cl.getResource("mytestfile.xhtml");
		
		try ( InputStream str = resource.openStream();
			  InputStreamReader reader = new InputStreamReader(str)) {
			
			String s = engine.createPage(reader, arguments);
			System.out.println("result="+s);
			
		}
	}
}
