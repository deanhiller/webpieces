package org.webpieces.regex;

import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.router.impl.RegExResult;
import org.webpieces.router.impl.RegExUtil;

public class TestRegEx {

	@Test
	public void testRegEx() {
		String path = "/path/{id}/user";
		RegExResult parsePath = RegExUtil.parsePath(path);
		
		Pattern pattern = Pattern.compile(parsePath.regExToMatch);
		Assert.assertTrue(pattern.matcher("/path/myid/user").matches());
		
		Assert.assertFalse(pattern.matcher("/something/path/myid/user").matches());
		Assert.assertFalse(pattern.matcher("/path/myid/user/ending").matches());
		Assert.assertFalse(pattern.matcher("/path/my/id/user").matches());
		Assert.assertFalse(pattern.matcher("/path//myid/user").matches());
		Assert.assertFalse(pattern.matcher("/path/asdf/myid/user").matches());
		
		Assert.assertEquals("id", parsePath.argNames.get(0));
	}

	@Test
	public void testTwo() {
		String path = "/path/{id}/user/{user}";
		RegExResult parsePath = RegExUtil.parsePath(path);
		
		Pattern pattern = Pattern.compile(parsePath.regExToMatch);
		Assert.assertTrue(pattern.matcher("/path/myid/user/user").matches());
		Assert.assertTrue(pattern.matcher("/path/myid/user/userA").matches());
		
		Assert.assertFalse(pattern.matcher("/something/path/myid/user").matches());
		Assert.assertFalse(pattern.matcher("/path/myid/user/ending/asdf").matches());
		Assert.assertFalse(pattern.matcher("/path/myid/user/asdf/").matches());
		
		Assert.assertEquals("id", parsePath.argNames.get(0));
		Assert.assertEquals("user", parsePath.argNames.get(1));
	}
	
//	
//	@Test
//	public void testRegEx() {
//		String line = "GET /path/{id}/{user}    Controller.{id}method({id})";
//		
//	    Pattern routePattern = new Pattern("^({method}GET|POST|PUT|DELETE|OPTIONS|HEAD|WS|\\*)[(]?({headers}[^)]*)(\\))?\\s+({path}.*/[^\\s]*)\\s+({action}[^\\s(]+)({params}.+)?(\\s*)$");
//
//	    Matcher matcher1 = routePattern.matcher(line);
//	    if(!matcher1.matches())
//	    	throw new IllegalArgumentException("pattern not match");
//	    
//	    String action = matcher1.group("action");
//        String method = matcher1.group("method");
//        String path = matcher1.group("path");
//        String params = matcher1.group("params");
//        String headers = matcher1.group("headers");
//        
//        String patternString = path;
//
//	    //String patternString = "/path/{id}";
//		Pattern customRegexPattern = new Pattern("\\{([a-zA-Z_][a-zA-Z_0-9]*)\\}");
//		patternString = customRegexPattern.replacer("\\{<[^/]+>$1\\}").replace(patternString);
//		
//		Pattern argsPattern = new Pattern("\\{<([^>]+)>([a-zA-Z_0-9]+)\\}");
//
//		List<Arg> args = new ArrayList<>();
//		Matcher matcher = argsPattern.matcher(patternString);
//        while (matcher.find()) {
//            Arg arg = new Arg();
//            arg.name = matcher.group(2);
//            System.out.println("name="+arg.name);
//            arg.constraint = new Pattern(matcher.group(1));
//            args.add(arg);
//        }
//		
//        System.out.println("pstring="+patternString);
//        
//        patternString = argsPattern.replacer("({$2}$1)").replace(patternString);
//        
//		System.out.println("pstring="+patternString);
//		
//		List<String> actionArgs = new ArrayList<String>(3);
//		// Action pattern
//        patternString = action;
//        patternString = patternString.replace(".", "[.]");
//        for (Arg arg : args) {
//        	System.out.println("checking arg="+arg.name+" on pstring="+patternString);
//            if (patternString.contains("{" + arg.name + "}")) {
//            	System.out.println("pattern contains="+arg.name);
//                patternString = patternString.replace("{" + arg.name + "}", "({" + arg.name + "}" + arg.constraint.toString() + ")");
//                actionArgs.add(arg.name);
//            } else
//            	System.out.println("nonono");
//        }
//        
//        System.out.println("latest action pattern="+patternString);
//        Pattern actionPattern = new Pattern(patternString, REFlags.IGNORE_CASE);
//	}
//	
//	private class Arg {
//
//		public Pattern constraint;
//		public String name;
//		
//	}
}
