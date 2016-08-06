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
		String tagArgs = fetchArgs(token);
		sourceCode.println("_attrs" + uniqueId + " = [" + tagArgs + "];");
		sourceCode.println("runTag('" + name + "', _attrs" + uniqueId + ", null, '"+token.getSourceLocation()+"');");
	}

	@Override
	public void generateStart(ScriptOutput sourceCode, Token token) {
		String tagArgs = fetchArgs(token);
		
		sourceCode.println("_attrs" + uniqueId + " = [" + tagArgs + "];");
		sourceCode.println("_body" + uniqueId + " = { // "+token.getSourceLocation());
	}

	private String fetchArgs(Token token) {
		String expr = token.getCleanValue();
		int indexOfSpace = expr.indexOf(" ");
		String tagArgs;
		if(indexOfSpace > 0) {
        	tagArgs = expr.substring(indexOfSpace + 1).trim();
            if (!tagArgs.matches("^[_a-zA-Z0-9]+\\s*:.*$")) {
            	//this is for the form #{tag 'something'} or #{tag variable}
                tagArgs = "_arg:" + tagArgs; 
            }
		} else {
			tagArgs = ":";
		}
		return tagArgs;
	}

	@Override
	public void generateEnd(ScriptOutput sourceCode, Token token) {
		String sourceLocation = startToken.getSourceLocation();
		sourceCode.println("}; // "+token.getSourceLocation()); // close body closure
		sourceCode.println("runTag('" + name + "', _attrs" + uniqueId + ", _body" + uniqueId + ", '"+sourceLocation+"');");
	}

}
