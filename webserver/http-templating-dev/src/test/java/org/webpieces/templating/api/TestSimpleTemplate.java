package org.webpieces.templating.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.templating.impl.DevTemplateService;
import org.webpieces.templating.impl.GroovyTemplateSuperclass;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestSimpleTemplate {

	private DevTemplateService svc;

	@Before
	public void setup() {
		Injector injector = Guice.createInjector(new ProdTemplateModule(new TemplateConfig()));
		svc = injector.getInstance(DevTemplateService.class);
	}
	
	@Test
	public void testBasicTemplate() throws IOException {
		Template template = svc.loadTemplate("/mytestfile.html");
		Map<String, Object> properties = createArgs(new UserBean("Dean Hiller"));
		StringWriter out = new StringWriter();
		svc.runTemplate(template, out, properties, null);
		
		//NOTE: We should be able to run with UserBean2 as well(this shows if
		//a Class was recompiled on-demand with our runtimecompiler we won't have issues in development mode
		Map<String, Object> args = createArgs(new UserBean2("Cooler Guy"));
		svc.runTemplate(template, new StringWriter(), args, null);
		
		System.out.println("HTML=\n"+out.toString());
	}

	@Test
	public void testWithPackage() throws IOException {
		Template template = svc.loadTemplate("/org/webpieces/mytestfile.html");
		Map<String, Object> properties = createArgs(new UserBean("Dean Hiller"));
		StringWriter out = new StringWriter();
		svc.runTemplate(template, out, properties, null);
		
		//NOTE: We should be able to run with UserBean2 as well(this shows if
		//a Class was recompiled on-demand with our runtimecompiler we won't have issues in development mode
		Map<String, Object> args = createArgs(new UserBean2("Cooler Guy"));
		svc.runTemplate(template, new StringWriter(), args, null);
		
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
