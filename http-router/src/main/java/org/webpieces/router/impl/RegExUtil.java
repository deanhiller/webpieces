package org.webpieces.router.impl;

import java.util.ArrayList;
import java.util.List;

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
			argNames.add(varName);
			String lastPart = regEx.substring(next+1);
			
			regEx = firstPart + "(?<"+varName+">[^/]+)" + lastPart;
		}
	}
}
