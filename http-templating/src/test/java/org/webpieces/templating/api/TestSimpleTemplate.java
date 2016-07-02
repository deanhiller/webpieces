package org.webpieces.templating.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.templating.impl.source.GroovyScriptGenerator;
import org.webpieces.templating.impl.source.ScriptCode;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestSimpleTemplate {

	private GroovyScriptGenerator srcGen;
	private TemplateEngine engine;

	@Before
	public void setup() {
		Injector injector = Guice.createInjector(new TemplateModule());
		srcGen = injector.getInstance(GroovyScriptGenerator.class);
		engine = injector.getInstance(TemplateEngine.class);
	}
	
	@Test
	public void testTemp() throws IOException {
		ClassLoader cl = TestSimpleTemplate.class.getClassLoader();
		URL resource = cl.getResource("mytestfile.xhtml");
		
		try (InputStream str = resource.openStream()) {
			String source = IOUtils.toString(str);
			ScriptCode sourceResult = srcGen.generate(source, "Template_MyClass");
		
			System.out.println("result=\n"+sourceResult.getScriptSourceCode());
			System.out.println("map="+sourceResult.getLineMapping());

			Map<String, Object> properties = new HashMap<>();
			properties.put("user", "Dean Hiller");
			properties.put("name", "dean");
			properties.put("color", "green");
			
			Template template = engine.createTemplate("MyTestGroovy", source);
			StringWriter out = new StringWriter();
			template.run(properties, out);
			
			System.out.println("HTML=\n"+out);
		}
	}
	
//	@Test
//	public void testBasic() throws IOException {
//		TemplateEngine engine = TemplatingFactory.create();
//		
//		engine.createGroovySource("org/webpieces/templating/api/simpleTemplate.html");
//		
//		
//		
//		
//		
//		UserBean userBean = new UserBean();
//		userBean.setName("Dean");
//		userBean.setNumSiblings(2);
//		Map<String, Object> arguments = new HashMap<>();
//		arguments.put("name", "Dean");
//		arguments.put("color", "blue");
//		arguments.put("user", userBean);
//		
//		ClassLoader cl = TestSimpleTemplate.class.getClassLoader();
//		URL resource = cl.getResource("mytestfile.xhtml");
//		
//		try ( InputStream str = resource.openStream();
//			  InputStreamReader reader = new InputStreamReader(str)) {
//			
//			String s = engine.createPage(reader, arguments);
//			System.out.println("result="+s);
//			
//		}
//	}
}
