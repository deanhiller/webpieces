package org.webpieces.router.impl.routebldr;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.webpieces.util.net.URLEncoder;

public class RegExUtil {
	private static final String SLASH_REGEX = "[/]{0,1}";

	public static RegExResult parsePath(String path1) {
		if(path1.endsWith("/")) {
			path1 = path1.substring(0, path1.length() - 1);
		}

		String regEx = path1;
		List<String> argNames = new ArrayList<>();
		while(true) {
			int index = regEx.indexOf("{");
			if(index < 0) {
				return new RegExResult(regEx + SLASH_REGEX, argNames);
			}

			int next = regEx.indexOf("}", index);
			if(next < 0)
				throw new IllegalArgumentException("There is no matching } for the { found in path="+path1);

			String firstPart = regEx.substring(0, index);
			String varName = regEx.substring(index+1, next);
			
			validateName(varName);
			
			argNames.add(varName);
			String lastPart = regEx.substring(next+1);
			
			regEx = firstPart + "(?<"+varName+">[^/]+)" + lastPart;
		}
	}

	private static void validateName(String varName) {
		String encodedName = URLEncoder.encode(varName, StandardCharsets.UTF_8);
		if(!varName.equals(encodedName))
			throw new IllegalArgumentException("The variable name="+varName+" is not correct and would need to be url encoded."
					+ "  Please only use normal variable names allowing safe javascript");
	}
}
