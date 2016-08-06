package org.webpieces.util.file;

public class ClassUtil {

	public static String translate(String packageContext, String relativePathToResource) {
		String[] split = relativePathToResource.split("/");
		for(int i = 0; i < split.length-1; i++) {
			String partialPath = split[i];
			if("..".equals(partialPath)) {
				packageContext = stripOneOff(packageContext);
			} else {
				packageContext = append(packageContext, partialPath);
			}
		}
		
		return packageContext + "." + split[split.length-1];
	}

	private static String append(String packageContext, String partialPath) {
		if("".equals(packageContext))
			return partialPath;
		
		return packageContext + "." + partialPath;
	}

	private static String stripOneOff(String packageContext) {
		int lastIndexOf = packageContext.lastIndexOf(".");
		if(lastIndexOf > 0) {
			return packageContext.substring(0, lastIndexOf);
		} else if(!"".equals(packageContext)) {
			return "";
		} else 
			throw new IllegalArgumentException("Too many .. were used in the path and we ended up going past the root classpath");
	}
}
