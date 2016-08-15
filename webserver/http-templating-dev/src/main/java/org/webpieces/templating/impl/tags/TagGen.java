package org.webpieces.templating.impl.tags;

import org.webpieces.templating.api.CompileCallback;
import org.webpieces.templating.api.GroovyGen;
import org.webpieces.templating.api.ScriptOutput;
import org.webpieces.templating.api.Token;

public class TagGen extends ParseTagArgs implements GroovyGen {

	private String name;
	private Token startToken;

	public TagGen(String tagName, Token startToken) {
		this.name = tagName;
		this.startToken = startToken;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void generateStartAndEnd(ScriptOutput sourceCode, Token token, int uniqueId, CompileCallback callback) {
		super.generateStartAttrs(sourceCode, token, uniqueId, callback);
		sourceCode.print("runTag('" + name + "', _attrs" + uniqueId + ", null, '"+token.getSourceLocation(false)+"');");
		sourceCode.appendTokenComment(token);
		sourceCode.println();
	}

	@Override
	public void generateStart(ScriptOutput sourceCode, Token token, int uniqueId, CompileCallback callback) {
		super.generateStartAttrs(sourceCode, token, uniqueId, callback);
		sourceCode.print("_body" + uniqueId + " = {");
		sourceCode.appendTokenComment(token);
		sourceCode.println();
	}

	@Override
	public void generateEnd(ScriptOutput sourceCode, Token token, int uniqueId) {
		sourceCode.print("};"); //close _body closure
		sourceCode.appendTokenComment(token);
		sourceCode.println();
		
		String sourceLocation = startToken.getSourceLocation(false);
		sourceCode.print("runTag('" + name + "', _attrs" + uniqueId + ", _body" + uniqueId + ", '"+sourceLocation+"');");
		sourceCode.appendTokenComment(token);
		sourceCode.println();
	}

}
