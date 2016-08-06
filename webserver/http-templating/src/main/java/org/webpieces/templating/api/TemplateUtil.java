package org.webpieces.templating.api;

public class TemplateUtil {

	public static String convertTemplateClassToPath(String fullClass) {
		//1. add prefix / as we always get it as a resource and that makes it an absolute path
		//replace all package . with / and replace _ with the . for the file extension...
		return "/"+fullClass.replace(".", "/").replace("_", ".");
	}
	
	public static String convertTemplatePathToClass(String fullPath) {
		//strip off the prefix '/'
		String className = fullPath.substring(1);
		//replace the one extension . with the _ then replace all / with package .
		return className.replace(".", "_").replace("/", ".");
	}
}
