package org.webpieces.templating.impl.tags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.templating.api.GroovyGen;
import org.webpieces.templating.api.ScriptOutput;
import org.webpieces.templating.api.Token;
import org.webpieces.templating.impl.source.TokenImpl;

public class TagGen implements GroovyGen {

	private static final Logger log = LoggerFactory.getLogger(TagGen.class);
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
            
            //TODO: record the tag used and source location to be verified at build time(ie. test will verify)
        	while(tagArgs.contains("@")) {
        		log.warn("need to record tag to file for testing validity at test time");
        		tagArgs = replaceRouteIds(token, tagArgs, indexOfSpace);
        	}
            
		} else {
			tagArgs = ":";
		}
		return tagArgs;
	}

	private String replaceRouteIds(Token token, String tagArgs, int indexOfSpace) {
		int atIndex = tagArgs.indexOf("@");
		int indexOfLeftParen = tagArgs.indexOf("[", atIndex);
		int indexOfRightParen = tagArgs.indexOf("]", indexOfLeftParen);
		int indexOfColon = tagArgs.indexOf(":", indexOfLeftParen);
		int nextAtIndex = tagArgs.indexOf("@", atIndex+1);

		int atLocation = atIndex+ 1 + indexOfSpace+1;
		if(indexOfLeftParen < 0) 
			throw new IllegalArgumentException(getMessage(token, atLocation, "Missing Left Bracket after @ROUTE"));
		else if(indexOfRightParen < 0)
			throw new IllegalArgumentException(getMessage(token, atLocation, "Missing Right Bracket after @ROUTE"));
		else if(indexOfColon < 0) 
			throw new IllegalArgumentException(getMessage(token, atLocation, "Missing : as you must have @{route}{groovymap} where an empty groovy map is [:] in the expression"));
		else if(nextAtIndex > 0) {
			if(indexOfLeftParen > nextAtIndex)
				throw new IllegalArgumentException(getMessage(token, atLocation, "Missing Left Bracket after @ROUTE"));
			else if(indexOfRightParen > nextAtIndex)
				throw new IllegalArgumentException(getMessage(token, atLocation, "Missing Right Bracket after @ROUTE"));
		}

		String prefix = tagArgs.substring(0, atIndex);
		String route = tagArgs.substring(atIndex+1, indexOfLeftParen).trim();
		String args = tagArgs.substring(indexOfLeftParen, indexOfRightParen+1);
		String leftover = tagArgs.substring(indexOfRightParen+1);

		log.warn("need to record the route here with argument names by splitting on : and , and can do some trivial validation on that as well and fail early");
		String groovy = prefix + "fetchUrl('"+route+"', "+args+", '"+token.getSourceLocation()+"')" + leftover;
		return groovy;
	}

	private String getMessage(Token token, int atIndex, String errorMsg) {
		return "Expression='"+token.getCleanValue()+"' has an error with @ token at position="
				+atIndex+" in the expression in this msg.  Error='"+errorMsg+"'  "+token.getSourceLocation();
	}

	@Override
	public void generateEnd(ScriptOutput sourceCode, Token token) {
		String sourceLocation = startToken.getSourceLocation();
		sourceCode.println("}; // "+token.getSourceLocation()); // close body closure
		sourceCode.println("runTag('" + name + "', _attrs" + uniqueId + ", _body" + uniqueId + ", '"+sourceLocation+"');");
	}

}
