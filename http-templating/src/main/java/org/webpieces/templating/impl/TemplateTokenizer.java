package org.webpieces.templating.impl;

import java.util.List;

public class TemplateTokenizer {

	public List<Token> tokenize(String source) {
		return new TokenizeHelper(source).parseSource();
	}
	
}
