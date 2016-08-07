package org.webpieces.templating.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TemplateUtil {

	public static String convertTemplateClassToPath(String fullClass) {
		if(fullClass == null)
			return null;
		//1. add prefix / as we always get it as a resource and that makes it an absolute path
		//replace all package . with / and replace _ with the . for the file extension...
		return "/"+fullClass.replace(".", "/").replace("_", ".");
	}
	
	public static String convertTemplatePathToClass(String fullPath) {
		if(fullPath == null)
			return null;
		
		//strip off the prefix '/'
		String className = fullPath.substring(1);
		//replace the one extension . with the _ then replace all / with package .
		return className.replace(".", "_").replace("/", ".");
	}
	
    public static String serialize(Map<?, ?> args, String... unless) {
    	Set<String> unlessSet = new HashSet<String>(Arrays.asList(unless));
    	unlessSet.add("_arg");
        StringBuilder attrs = new StringBuilder();
        for (Object key : args.keySet()) {
            String keyStr = key.toString();
            Object value = args.get(key);
            String valueStr = "";
            if(value != null)
            	valueStr = value.toString();
            if (!unlessSet.contains(keyStr)) {
            	attrs.append(" ");
                attrs.append(keyStr);
                attrs.append("=\"");
                attrs.append(valueStr);
                attrs.append("\"");
            }
        }
        return attrs.toString();
    }
    
}
