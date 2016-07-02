package org.webpieces.templating.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
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
	public void testBasicTemplate() throws IOException {
		ClassLoader cl = TestSimpleTemplate.class.getClassLoader();
		URL resource = cl.getResource("mytestfile.xhtml");
		
		try (InputStream str = resource.openStream()) {
			String source = IOUtils.toString(str);
			ScriptCode sourceResult = srcGen.generate(source, "Template_MyClass");
		
			System.out.println("result=\n"+sourceResult.getScriptSourceCode());
			System.out.println("map="+sourceResult.getLineMapping());

			Map<String, Object> properties = createArgs(new UserBean("Dean Hiller"));
			
			Template template = engine.createTemplate("MyTestGroovy", source);
			StringWriter out = new StringWriter();
			template.run(properties, out);

			//NOTE: We should be able to run with UserBean2 as well(this shows if
			//a Class was recompiled on-demand with our runtimecompiler we won't have issues in development mode
			Map<String, Object> args = createArgs(new UserBean2("Cooler Guy"));
			StringWriter out2 = new StringWriter();
			template.run(args, out2);
			
			System.out.println("HTML=\n"+out);
		}
	}

	private Map<String, Object> createArgs(Object user) {
		Map<String, Object> properties = new HashMap<>();
		properties.put("user", user);
		properties.put("color", "green");
		return properties;
	}
	
}
