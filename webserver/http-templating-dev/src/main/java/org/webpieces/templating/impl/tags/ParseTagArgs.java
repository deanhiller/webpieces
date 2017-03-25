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
		
		sourceCode.println("enterExpression('"+token.getSourceLocation(false)+"');", token); //purely so we can add info to missing properties
		sourceCode.println("_attrs" + uniqueId + " = [" + tagArgs + "];", token);
		sourceCode.println("exitExpression();", token);
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
            
            //record the tag used and source location to be verified at unit test time
        	while(tagArgs.contains("@[")) {
        		tagArgs = replaceRouteIds(token, tagArgs, indexOfSpace, callback);
        	}
            
        	while(tagArgs.contains("%[")) {
        		tagArgs = replacePaths(token, tagArgs, indexOfSpace, callback);
        	}
        	
        	
		} else {
			tagArgs = ":";
		}
		return tagArgs;
	}

	private String replacePaths(Token token, String tagArgs, int indexOfSpace, CompileCallback callback) {
		int atIndex = tagArgs.indexOf("%[");
		int nextAtIndex = tagArgs.indexOf("]%");
		if(nextAtIndex < 0)
			throw new IllegalArgumentException("Missing closing ]% on the route."+token.getSourceLocation(true));

		String prefix = tagArgs.substring(0, atIndex);
		String relativeUrlPath = tagArgs.substring(atIndex+2, nextAtIndex);
		String leftover = tagArgs.substring(nextAtIndex+2);
		
		String groovyCode = translatePath(relativeUrlPath, token, callback);
		
		String groovy = prefix + groovyCode + leftover;
		return groovy;
	}

	private String translatePath(String relativeUrlPath, Token token, CompileCallback callback) {
		
		callback.recordPath(relativeUrlPath, token.getSourceLocation(false));

		return relativeUrlPath;
	}

	public static String replaceRouteIds(Token token, String tagArgs, int indexOfSpace, CompileCallback callback) {
		int atIndex = tagArgs.indexOf("@[");
		int nextAtIndex = tagArgs.indexOf("]@");
		if(nextAtIndex < 0)
			throw new IllegalArgumentException("Missing closing ]@ on the route."+token.getSourceLocation(true));

		String prefix = tagArgs.substring(0, atIndex);
		String routeInfo = tagArgs.substring(atIndex+2, nextAtIndex);
		String leftover = tagArgs.substring(nextAtIndex+2);
		
		String groovyCode = translateRouteId(routeInfo, token, callback);
		
		String groovy = prefix + groovyCode + leftover;
		return groovy;
	}

	public static String translateRouteId(String routeText, Token token, CompileCallback callback) {

		int firstCommaLocation = routeText.indexOf(",");
		String route;
		String args;
		if(firstCommaLocation > 0) {
			route = routeText.substring(0, firstCommaLocation);
			args = routeText.substring(firstCommaLocation+1);
			args = "["+args+"]";
		} else {
			route = routeText;
			args = "[:]";
		}

		//TODO: This is not proper as it will break if there is a Map in a Map...but it works for now on validating the key names
		//add tests eventually and fix
		List<String> argNames = fetchArgNames(args, token);
		callback.recordRouteId(route, argNames, token.getSourceLocation(false));
		
		return "fetchUrl('"+route+"', "+args+", '"+token.getSourceLocation(false)+"')";
	}
	
	private static List<String> fetchArgNames(String args, Token token) {
		List<String> names = new ArrayList<>();
		if("[:]".equals(args))
			return names;
			
		String noBracketsArgs = args.substring(1, args.length()-1);
		String[] split = noBracketsArgs.split("[:,]");
		if(split.length % 2 != 0)
			throw new IllegalArgumentException("One of a few issues occurred with your code\n"
					+ " 1. You forgot to close with ]@ token.  The body of the token looks like this(if this looks wrong, fix it)='''"+token.getCleanValue()+"'''\n"
					+ " 2. The groovy Map appears to be invalid as splitting on [:,] results in"
					+ " an odd amount of elements and it shold be key:value,key2:value "+token.getSourceLocation(true));
		for(int i = 0; i < split.length; i+=2) {
			names.add(split[i]);
		}
		
		return names;
	}

}
