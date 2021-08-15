package org.webpieces.templatingdev.impl.tags;

import org.webpieces.templatingdev.api.ScriptOutput;
import org.webpieces.templatingdev.api.Token;

public class TagGen extends ParseTagArgs {

	private String name;
	private Token startToken;

	public TagGen(String tagName, Token startToken, RoutePathTranslator callback) {
		super(callback);
		this.name = tagName;
		this.startToken = startToken;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void generateStartAndEnd(ScriptOutput sourceCode, Token token, int uniqueId) {
		super.generateStartAttrs(sourceCode, token, uniqueId);
		sourceCode.println("runTag('" + name + "', _attrs" + uniqueId + ", null, '"+token.getSourceLocation(false)+"');", token);
		sourceCode.println();
	}

	@Override
	public void generateStart(ScriptOutput sourceCode, Token token, int uniqueId) {
		super.generateStartAttrs(sourceCode, token, uniqueId);
		sourceCode.println("_body" + uniqueId + " = {", token);
		sourceCode.println();
	}

	@Override
	public void generateEnd(ScriptOutput sourceCode, Token token, int uniqueId) {
		sourceCode.println("};", token); //close _body closure
		sourceCode.println();
		
		String sourceLocation = startToken.getSourceLocation(false);
		sourceCode.println("runTag('" + name + "', _attrs" + uniqueId + ", _body" + uniqueId + ", '"+sourceLocation+"');", token);
		sourceCode.println();
	}

}
