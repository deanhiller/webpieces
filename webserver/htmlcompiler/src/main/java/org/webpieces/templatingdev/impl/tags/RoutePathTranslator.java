package org.webpieces.templatingdev.impl.tags;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.webpieces.templatingdev.api.CompileCallback;
import org.webpieces.templatingdev.api.Token;

public class RoutePathTranslator {

	private CompileCallback callback;

	@Inject
	public RoutePathTranslator(CompileCallback callback) {
		this.callback = callback;
	}
	
	public String translateRouteId(String routeText, Token token) {

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

	public String recordPath(String relativeUrlPath, String sourceLocation) {
		callback.recordPath(relativeUrlPath, sourceLocation);
		return relativeUrlPath;
	}
}
