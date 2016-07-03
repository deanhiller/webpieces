package org.webpieces.router.impl.loader;

public class ResolvedMethod {

	private String methodStr;
	private String controllerStr;

	public ResolvedMethod(String controllerStr, String methodStr) {
		this.controllerStr = controllerStr;
		this.methodStr = methodStr;
	}

	public String getMethodStr() {
		return methodStr;
	}

	public String getControllerStr() {
		return controllerStr;
	}
	
}
