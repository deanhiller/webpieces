package org.webpieces.router.impl.dto;

//A view is one of a few references
// - A template reference of form /my/directory/myTemplate.html in which 
//      - (for development) /my/directory matches the package of the controller
//      - (for development) myTemplate matches the method name that was called
//      - (for development) my.directory.__myTemplate_html is the classname to give the new class
//      - (for production) /my/directory/myTemplate.html becomes my.directory.__myTemplate_html Class file
// - A static file reference
public class View {

	private String controllerName;
	private String methodName;
	private String relativeOrAbsolutePath;

	public View(String controllerName, String methodName, String relativeOrAbsolutePath) {
		this.controllerName = controllerName;
		this.methodName = methodName;
		this.relativeOrAbsolutePath = relativeOrAbsolutePath;
	}

	public String getControllerName() {
		return controllerName;
	}

	public String getRelativeOrAbsolutePath() {
		return relativeOrAbsolutePath;
	}

	public String getPackageName() {
		int index = controllerName.lastIndexOf(".");
		if(index < 0)
			return "";
		
		String pack = controllerName.substring(0, index);
		return pack;
	}

	public String getMethodName() {
		return methodName;
	}
}
