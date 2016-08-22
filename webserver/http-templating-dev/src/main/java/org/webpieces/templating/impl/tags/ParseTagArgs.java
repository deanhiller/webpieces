package org.webpieces.templating.impl.tags;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.templating.api.CompileCallback;
import org.webpieces.templating.api.GroovyGen;
import org.webpieces.templating.api.ScriptOutput;
import org.webpieces.templating.api.Token;

public abstract class ParseTagArgs implements GroovyGen {
	
	protected void generateStartAttrs(ScriptOutput sourceCode, Token token, int uniqueId, CompileCallback callback) {
		String tagArgs = fetchArgs(token, callback);
		sourceCode.println("_attrs" + uniqueId + " = [" + tagArgs + "];", token);
		sourceCode.println();
	}

	private String fetchArgs(Token token, CompileCallback callback) {
		String expr = token.getCleanValue();
		int indexOfSpace = expr.indexOf(" ");
		String tagArgs;
		if(indexOfSpace > 0) {
        	tagArgs = expr.substring(indexOfSpace + 1).trim();
            if (!tagArgs.matches("^[_a-zA-Z0-9]+\\s*:.*$")) {
            	//this is for the form #{tag 'something'} or #{tag variable}
                tagArgs = "defaultArgument:" + tagArgs; 
            }
            
            //TODO: record the tag used and source location to be verified at build time(ie. test will verify)
        	while(tagArgs.contains("@[")) {
        		tagArgs = replaceRouteIds(token, tagArgs, indexOfSpace, callback);
        	}
            
		} else {
			tagArgs = ":";
		}
		return tagArgs;
	}

	private String replaceRouteIds(Token token, String tagArgs, int indexOfSpace, CompileCallback callback) {
		int atIndex = tagArgs.indexOf("@[");
		int nextAtIndex = tagArgs.indexOf("]@");
		if(nextAtIndex < 0)
			throw new IllegalArgumentException("Missing closing ]@ on the route."+token.getSourceLocation(true));

		int firstCommaLocation = tagArgs.indexOf(",", atIndex);
		String prefix = tagArgs.substring(0, atIndex);
		String leftover = tagArgs.substring(nextAtIndex+2);
		String route;
		String args;
		if(firstCommaLocation > 0 && firstCommaLocation < nextAtIndex) {
			route = tagArgs.substring(atIndex+2, firstCommaLocation);
			args = tagArgs.substring(firstCommaLocation+1, nextAtIndex);
			args = "["+args+"]";
		} else {
			route = tagArgs.substring(atIndex+2, nextAtIndex);
			args = "[:]";
		}
		
		//TODO: This is not proper as it will break if there is a Map in a Map...but it works for now on validating the key names
		List<String> argNames = fetchArgNames(args, token);
		callback.routeIdFound(route, argNames, token.getSourceLocation(false));
		
		String groovy = prefix + "fetchUrl('"+route+"', "+args+", '"+token.getSourceLocation(false)+"')" + leftover;
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
					+ " an odd amount of elements and it shold be key:value,key2:value "+token.getSourceLocation(true));
		for(int i = 0; i < split.length; i+=2) {
			names.add(split[i]);
		}
		
		return names;
	}

}
