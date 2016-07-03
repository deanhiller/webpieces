package org.webpieces.templating.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.webpieces.templating.api.Template;
import org.webpieces.templating.api.TemplateEngine;
import org.webpieces.templating.api.TemplateService;

public class DevTemplateService implements TemplateService {

	private TemplateEngine engine;

	@Inject
	public DevTemplateService(TemplateEngine engine) {
		this.engine = engine;
	}

	@Override
	public Template loadTemplate(String packageStr, String templateClassName, String extension) {
		try {
			return loadTemplateImpl(packageStr, templateClassName, extension);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Template loadTemplateImpl(String packageStr, String templateClassName, String extension) throws IOException {
		String directory = "/"+packageStr.replace(".", "/");
		String fileName = templateClassName+"."+extension;

		InputStream resource = DevTemplateService.class.getResourceAsStream(directory+"/"+fileName);
		if(resource == null)
			throw new FileNotFoundException("resource="+directory+"/"+fileName+" was not found in classpath");

		String viewSource = IOUtils.toString(resource, Charset.defaultCharset().name());

		String fullClassName = packageStr+"."+templateClassName+"_"+extension;

		return engine.createTemplate(fullClassName, viewSource);
	}

}
