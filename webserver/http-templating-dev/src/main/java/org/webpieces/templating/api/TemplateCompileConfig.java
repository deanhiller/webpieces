package org.webpieces.templating.api;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class TemplateCompileConfig {

	private Charset fileEncoding = StandardCharsets.UTF_8; 

	public TemplateCompileConfig() {
	}
	
	public TemplateCompileConfig(Charset fileEncoding) {
		this.fileEncoding = fileEncoding;
	}

	public Charset getFileEncoding() {
		return fileEncoding;
	}

	public void setFileEncoding(Charset fileEncoding) {
		this.fileEncoding = fileEncoding;
	}

}
