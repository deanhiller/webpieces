package org.webpieces.templating.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.templating.impl.source.GroovyScriptGenerator;
import org.webpieces.templating.impl.source.SourceState;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestSimpleTemplate {

	private GroovyScriptGenerator srcGen;

	@Before
	public void setup() {
		Injector injector = Guice.createInjector(new TemplateModule());
		srcGen = injector.getInstance(GroovyScriptGenerator.class);
	}
	
	@Test
	public void testTemp() throws IOException {
		ClassLoader cl = TestSimpleTemplate.class.getClassLoader();
		URL resource = cl.getResource("mytestfile.xhtml");
		
		try (InputStream str = resource.openStream()) {
			String source = IOUtils.toString(str);
			SourceState sourceResult = srcGen.generate(source, "Template_MyClass");
			
			System.out.println("result=\n"+sourceResult.getScriptSourceCode());
			System.out.println("map="+sourceResult.getLineMapping());	
		}
	}
	
	@Test
	public void testBasic() throws IOException {
		TemplateEngine engine = TemplatingFactory.create();
		
		engine.createGroovySource("org/webpieces/templating/api/simpleTemplate.html");
		
		
		
		
		
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
