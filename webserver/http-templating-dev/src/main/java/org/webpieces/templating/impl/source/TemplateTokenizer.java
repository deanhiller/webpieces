package org.webpieces.templating.impl.source;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TemplateTokenizer {

	private Set<TemplateToken> tagsToCleanWhitespace = new HashSet<>();
	
	public TemplateTokenizer() {
		tagsToCleanWhitespace.add(TemplateToken.START_TAG);
		tagsToCleanWhitespace.add(TemplateToken.END_TAG);
		tagsToCleanWhitespace.add(TemplateToken.START_END_TAG);
		tagsToCleanWhitespace.add(TemplateToken.COMMENT);
	}
	
	public List<TokenImpl> tokenize(String filePath, String source) {
		List<TokenImpl> tokens = new TemplateTokenizerTask(filePath, source).parseSource();
		return optimize(tokens);
	}

	//This did not work as ${_body}$ on one line became not well formatted in #{field}
	//instead just drop any pretty printing as it is not really worth the effort maybe(it would be nice though)
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
			if(tagsToCleanWhitespace.contains(token.state) && left.state == TemplateToken.PLAIN && right.state == TemplateToken.PLAIN) {
				if("".equals(left.getValue().trim()) && "".equals(right.getValue().trim())) {
					tokens.remove(i+1);
					if(token.state == TemplateToken.COMMENT) {
						tokens.remove(i); //remove the actual token as well 
					}
					tokens.remove(i-1);
					if(i >= tokens.size())
						i--;
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