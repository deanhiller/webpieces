package org.webpieces.templating.impl.tags;

import org.webpieces.templating.api.GroovyGen;
import org.webpieces.templating.api.ScriptOutput;
import org.webpieces.templating.api.Token;

public class TagGen implements GroovyGen {

	private String name;
	private int uniqueId;
	private Token startToken;

	public TagGen(String tagName, Token startToken, int uniqueId) {
		this.name = tagName;
		this.uniqueId = uniqueId;
		this.startToken = startToken;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void generateStartAndEnd(ScriptOutput sourceCode, Token token) {
		generateStart(sourceCode, token);
		generateEnd(sourceCode, token);
	}

	@Override
	public void generateStart(ScriptOutput sourceCode, Token token) {
		String expr = token.getCleanValue();
		int indexOfSpace = expr.indexOf(" ");
		String tagArgs;
		if(indexOfSpace > 0) {
        	tagArgs = expr.substring(indexOfSpace + 1).trim();
            if (!tagArgs.matches("^[_a-zA-Z0-9]+\\s*:.*$")) {
                tagArgs = "_arg:" + tagArgs;
            }
		} else {
			tagArgs = ":";
		}
		
		sourceCode.println("_attrs" + uniqueId + " = [" + tagArgs + "];");
		sourceCode.println("_body" + uniqueId + " = { // "+token.getSourceLocation());
	}

	//(java.lang.String, java.util.LinkedHashMap, org.webpieces.webserver.basic.biz.getTag_html$_run_closure1$_closure2, org.webpieces.webserver.basic.biz.getTag_html, java.lang.String)
	@Override
	public void generateEnd(ScriptOutput sourceCode, Token token) {
		String sourceLocation = startToken.getSourceLocation();
		sourceCode.println("}; // "+token.getSourceLocation()); // close body closure
		sourceCode.println("runTag('" + name + "', _attrs" + uniqueId + ", _body" + uniqueId + ", '"+sourceLocation+"');");
	}

}
