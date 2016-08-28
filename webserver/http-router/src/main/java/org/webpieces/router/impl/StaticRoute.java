package org.webpieces.router.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.dto.RouteType;

public class StaticRoute implements Route {

	private String urlPath;
	private String fileSystemPath;

	private boolean isFile = false;
	private Pattern patternToMatch;
	private boolean isOnClassPath;
	private List<String> pathParamNames = new ArrayList<>();

	public StaticRoute(String urlPath, String fileSystemPath, boolean isOnClassPath) {
		this.fileSystemPath = fileSystemPath;
		this.isOnClassPath = isOnClassPath;
		
		//very big conflict between domain/path/path/path
		if(!urlPath.startsWith("/"))
			throw new IllegalArgumentException("static resource url paths must start with / and can't have domain name at this time="+urlPath);
		else if(isOnClassPath) {
			if(!fileSystemPath.startsWith("/"))
				throw new IllegalArgumentException("Classpath resources must start with a / and be absolute on the classpath");
		} else {//on filesystem
			if(!fileSystemPath.startsWith("/")) {
				String path = System.getProperty("user.dir");
				this.fileSystemPath = path + "/"+fileSystemPath;
			}
		}
		
		this.urlPath = urlPath;
		if(urlPath.endsWith("/")) {
			this.isFile = false;
			this.patternToMatch = Pattern.compile("^"+urlPath+"(?<resource>.*)$");
			this.pathParamNames.add("resource");
		} else {
			this.isFile = true;
			this.patternToMatch = Pattern.compile("^"+urlPath+"$");
		}
	}

	@Override
	public String getPath() {
		return urlPath;
	}

	@Override
	public boolean matchesMethod(HttpMethod method) {
		if(method == HttpMethod.GET || method == HttpMethod.HEAD)
			return true;
		return false;
	}

	@Override
	public Matcher matches(RouterRequest request, String subPath) {
		if(!matchesMethod(request.method)) {
			return null;
		}
		
		Matcher matcher = patternToMatch.matcher(subPath);
		return matcher;
	}

	@Override
	public String getControllerMethodString() {
		throw new UnsupportedOperationException("should not call this");
	}

	@Override
	public List<String> getPathParamNames() {
		return pathParamNames ;
	}

	@Override
	public RouteType getRouteType() {
		return RouteType.STATIC;
	}

	@Override
	public boolean isPostOnly() {
		return false;
	}

	@Override
	public boolean isCheckSecureToken() {
		return false;
	}

	public boolean getIsOnClassPath() {
		return isOnClassPath;
	}

	public String getFileSystemPath() {
		return fileSystemPath;
	}

	public String transform(Map<String, String> pathParams) {
		if(isFile)
			return fileSystemPath;

		String relativeUrlPathLeft = pathParams.get("resource");
		return fileSystemPath+relativeUrlPathLeft;
	}

}
