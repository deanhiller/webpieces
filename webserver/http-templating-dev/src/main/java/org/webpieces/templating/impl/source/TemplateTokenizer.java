package org.webpieces.templating.impl.source;

import java.util.List;

public class TemplateTokenizer {

	public List<TokenImpl> tokenize(String filePath, String source) {
		List<TokenImpl> tokens = new TempateTokenizerRunnable(filePath, source).parseSource();
		return optimize(tokens);
	}

	/*
	 * Only when we have
	 *  \n    #{tag}#    \n
	 *  then do we want to remove the PLAIN to the left AND to the right of the tag itself
	 */
	private List<TokenImpl> optimize(List<TokenImpl> tokens) {
		for(int i = tokens.size()-2; i >= 1; i--) {
			TokenImpl left = tokens.get(i-1);
			TokenImpl token = tokens.get(i);
			TokenImpl right = tokens.get(i+1);
			if(token.state != TemplateToken.PLAIN && left.state == TemplateToken.PLAIN && right.state == TemplateToken.PLAIN) {
				if("".equals(left.getValue().trim()) && "".equals(right.getValue().trim())) {
					tokens.remove(i+1);
					tokens.remove(i-1);
				}
			} else if(token.state == TemplateToken.PLAIN && "".equals(token.getValue())) {
				//remove tokens that are just "" as we don't need to print empty string out
				tokens.remove(i);
			}
		}
	
		//We could chain back together PLAIN tokens here as well...
	
		return tokens;
	}
	
}
