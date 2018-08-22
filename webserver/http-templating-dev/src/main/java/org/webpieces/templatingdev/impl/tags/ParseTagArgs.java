package org.webpieces.templatingdev.impl.tags;

import org.webpieces.templatingdev.api.GroovyGen;
import org.webpieces.templatingdev.api.ScriptOutput;
import org.webpieces.templatingdev.api.Token;
import org.webpieces.templatingdev.impl.source.TemplateToken;

public abstract class ParseTagArgs implements GroovyGen {
	
	private RoutePathTranslator callback;

	public ParseTagArgs(RoutePathTranslator callback) {
		this.callback = callback;
	}
	
	protected void generateStartAttrs(ScriptOutput sourceCode, Token token, int uniqueId) {
		String tagArgs = fetchArgs(token);
		
		sourceCode.println("enterExpression('"+token.getSourceLocation(false)+"');", token); //purely so we can add info to missing properties
		sourceCode.println("_attrs" + uniqueId + " = [" + tagArgs + "];", token);
		sourceCode.println("exitExpression();", token);
		sourceCode.println();
	}

	private String fetchArgs(Token token) {
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
        	while(tagArgs.contains(TemplateToken.ACTION.getStart())) {
        		tagArgs = replaceRouteIds(token, tagArgs, indexOfSpace);
        	}
            
        	while(tagArgs.contains("%[")) {
        		tagArgs = replacePaths(token, tagArgs, indexOfSpace);
        	}
        	
        	
		} else {
			tagArgs = ":";
		}
		return tagArgs;
	}

	private String replacePaths(Token token, String tagArgs, int indexOfSpace) {
		int atIndex = tagArgs.indexOf("%[");
		int nextAtIndex = tagArgs.indexOf("]%");
		if(nextAtIndex < 0)
			throw new IllegalArgumentException("Missing closing ]% on the route."+token.getSourceLocation(true));

		String prefix = tagArgs.substring(0, atIndex);
		String relativeUrlPath = tagArgs.substring(atIndex+2, nextAtIndex);
		String leftover = tagArgs.substring(nextAtIndex+2);
		
		String groovyCode = callback.recordPath(relativeUrlPath, token.getSourceLocation(false));
		
		String groovy = prefix + groovyCode + leftover;
		return groovy;
	}

	private String replaceRouteIds(Token token, String tagArgs, int indexOfSpace) {
		int atIndex = tagArgs.indexOf(TemplateToken.ACTION.getStart());
		int nextAtIndex = tagArgs.indexOf(TemplateToken.ACTION.getEnd());
		if(nextAtIndex < 0)
			throw new IllegalArgumentException("Missing closing ]@ on the route."+token.getSourceLocation(true));

		String prefix = tagArgs.substring(0, atIndex);
		String routeInfo = tagArgs.substring(atIndex+2, nextAtIndex);
		String leftover = tagArgs.substring(nextAtIndex+2);
		
		String groovyCode = callback.translateRouteId(routeInfo, token);
		
		String groovy = prefix + groovyCode + leftover;
		return groovy;
	}

}
