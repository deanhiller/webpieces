package org.webpieces.templating.api;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class TemplateConfig {

	private Charset defaultFormAcceptEncoding = StandardCharsets.UTF_8;
	private String fieldTagTemplatePath = "/org/webpieces/templating/impl/field.tag";
	private String arrayFieldTagTemplatePath = fieldTagTemplatePath;

	public TemplateConfig() {
	}
	
	public TemplateConfig(Charset defaultFormAcceptEncoding) {
		this.defaultFormAcceptEncoding = defaultFormAcceptEncoding;
	}
	
	public Charset getDefaultFormAcceptEncoding() {
		return defaultFormAcceptEncoding;
	}

	public void setDefaultFormAcceptEncoding(Charset defaultFormAcceptEncoding) {
		this.defaultFormAcceptEncoding = defaultFormAcceptEncoding;
	}

	public void setFieldTagTemplatePath(String path) {
		this.fieldTagTemplatePath = path;
	}
	
	public String getFieldTagTemplatePath() {
		return fieldTagTemplatePath;
	}

	public String getArrayFieldTagTemplatePath() {
		return arrayFieldTagTemplatePath;
	}

}
