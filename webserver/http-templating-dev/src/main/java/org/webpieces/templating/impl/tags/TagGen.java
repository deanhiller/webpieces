package org.webpieces.templating.impl.tags;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.templating.api.CompileCallback;
import org.webpieces.templating.api.GroovyGen;
import org.webpieces.templating.api.ScriptOutput;
import org.webpieces.templating.api.Token;

public class TagGen implements GroovyGen {

	private static final Logger log = LoggerFactory.getLogger(TagGen.class);
	private String name;
	private int uniqueId;
	private Token startToken;
	private CompileCallback callback;

	public TagGen(String tagName, Token startToken, int uniqueId, CompileCallback callback) {
		this.name = tagName;
		this.uniqueId = uniqueId;
		this.startToken = startToken;
		this.callback = callback;
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

		List<String> argNames = fetchArgNames(args, token);
		callback.routeIdFound(route, argNames, token.getSourceLocation());
		
		log.warn("need to record the route here with argument names by splitting on : and , and can do some trivial validation on that as well and fail early");
		String groovy = prefix + "fetchUrl('"+route+"', "+args+", '"+token.getSourceLocation()+"')" + leftover;
		return groovy;
	}

	private List<String> fetchArgNames(String args, Token token) {
		List<String> names = new ArrayList<>();
		if("[:]".equals(args))
			return names;
			
		String noBracketsArgs = args.substring(1, args.length()-1);
		String[] split = noBracketsArgs.split("[:,]");
		if(split.length % 2 != 0)
			throw new IllegalArgumentException("The groovy Map appears to be invalid as splitting on [:,] results in"
					+ " an odd amount of elements and it shold be key:value,key2:value "+token.getSourceLocation());
		for(int i = 0; i < split.length; i+=2) {
			names.add(split[i]);
		}
		
		return names;
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
