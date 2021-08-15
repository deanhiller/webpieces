package org.webpieces.templating.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.webpieces.templating.impl.GroovyTemplateSuperclass;
import org.webpieces.util.file.ClassUtil;

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
    	unlessSet.add("defaultArgument");
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

	public static String translateToProperFilePath(GroovyTemplateSuperclass callingTemplate, String superTemplatePath) {
		String className = translateToClassName(callingTemplate, superTemplatePath);
		return TemplateUtil.convertTemplateClassToPath(className);
	}
	
	private static String translateToClassName(GroovyTemplateSuperclass callingTemplate, String superTemplatePath) {
		if(superTemplatePath == null)
			return null;
		else if(superTemplatePath.startsWith("/"))
			return TemplateUtil.convertTemplatePathToClass(superTemplatePath);
		
		//get the package this template was in...
		String name = callingTemplate.getClass().getName();
		int lastIndexOf = name.lastIndexOf(".");
		String packageCtx = "";
		if(lastIndexOf > 0) {
			packageCtx = name.substring(0, lastIndexOf);
		}
		
		//had to do this since ClassUtil.translate deals in . and returns a classname so this makes it compatible..
		String superTemplatePathWithClassName = removeUnderDotFromFileName(superTemplatePath);
		String fullTempateClassName = ClassUtil.translate(packageCtx, superTemplatePathWithClassName);
		return fullTempateClassName;
	}

	private static String removeUnderDotFromFileName(String superTemplatePath) {
		int lastIndexOfSlash = superTemplatePath.lastIndexOf("/");
		String pathWithNoFile = "";
		String fileName = superTemplatePath;
		if(lastIndexOfSlash > 0) {
			pathWithNoFile = superTemplatePath.substring(0, lastIndexOfSlash);
			fileName = superTemplatePath.substring(lastIndexOfSlash);
		}
		fileName = fileName.replace(".", "_");
		
		String superTemplatePathWithClassName = pathWithNoFile+fileName;
		return superTemplatePathWithClassName;
	}
}
