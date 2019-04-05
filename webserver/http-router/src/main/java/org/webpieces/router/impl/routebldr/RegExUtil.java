package org.webpieces.router.impl.routebldr;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.webpieces.util.net.URLEncoder;

public class RegExUtil {

	public static RegExResult parsePath(String path1) {
		String regEx = path1;
		List<String> argNames = new ArrayList<>();
		while(true) {
			int index = regEx.indexOf("{");
			if(index < 0) {
				return new RegExResult(regEx, argNames);
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
