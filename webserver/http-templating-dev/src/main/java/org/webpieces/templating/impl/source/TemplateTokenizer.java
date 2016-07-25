package org.webpieces.templating.impl.source;

import java.util.List;

public class TemplateTokenizer {

	public List<Token> tokenize(String filePath, String source) {
		return new TempateTokenizerRunnable(filePath, source).parseSource();
	}
	
}
