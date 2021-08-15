package org.webpieces.templating.api;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.templatingdev.api.DevTemplateModule;
import org.webpieces.templatingdev.api.StubModule;
import org.webpieces.templatingdev.api.TemplateCompileConfig;
import org.webpieces.templatingdev.impl.DevTemplateService;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestSimpleTemplate {

	private DevTemplateService svc;

	@Before
	public void setup() {
		Injector injector = Guice.createInjector(
				new DevTemplateModule(new TemplateCompileConfig(false)),
				new StubModule()
				);
		svc = injector.getInstance(DevTemplateService.class);
	}

	@Test
	public void testGradlesAbilitytoLoadCorrectly() {
		URL resource = DevTemplateService.class.getResource("/mytestfile.html");

		Assert.assertNotNull(resource);
	}

	@Test
	public void testBasicTemplate() throws IOException {
		String templateName = "/mytestfile.html";
		Map<String, Object> properties = createArgs(new UserBean("Dean Hiller"));
		StringWriter out = new StringWriter();
		svc.loadAndRunTemplate(templateName, out, properties);
		
		//NOTE: We should be able to run with UserBean2 as well(this shows if
		//a Class was recompiled on-demand with our runtimecompiler we won't have issues in development mode
		Map<String, Object> args = createArgs(new UserBean2("Cooler Guy"));
		svc.loadAndRunTemplate(templateName, new StringWriter(), args);
		
		System.out.println("HTML=\n"+out.toString());
	}

	@Test
	public void testWithPackage() throws IOException {
		String templateName = "/org/webpieces/mytestfile.html";
		Map<String, Object> properties = createArgs(new UserBean("Dean Hiller"));
		StringWriter out = new StringWriter();
		svc.loadAndRunTemplate(templateName, out, properties);
		
		//NOTE: We should be able to run with UserBean2 as well(this shows if
		//a Class was recompiled on-demand with our runtimecompiler we won't have issues in development mode
		Map<String, Object> args = createArgs(new UserBean2("Cooler Guy"));
		svc.loadAndRunTemplate(templateName, new StringWriter(), args);
		
		String html = out.toString();
		Assert.assertTrue("Html was="+html, html.contains("Hi there, my name is Dean Hiller and my favorite color is green"));
	}
	
	private Map<String, Object> createArgs(Object user) {
		Map<String, Object> properties = new HashMap<>();
		properties.put("user", user);
		properties.put("color", "green");
		return properties;
	}
	
}
