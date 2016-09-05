package org.webpieces.router.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
	private int uniqueStaticRouteId;

	public StaticRoute(int staticRouteId, String urlPath, String fileSystemPath, boolean isOnClassPath) {
		this.uniqueStaticRouteId = staticRouteId;
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

		File f = new File(this.fileSystemPath);
		if(!f.exists())
			throw new IllegalArgumentException("File="+getCanonicalPath(f)+" does not exist");

		this.urlPath = urlPath;
		if(urlPath.endsWith("/")) {
			this.isFile = false;
			if(!fileSystemPath.endsWith("/"))
				throw new IllegalArgumentException("Static directory so fileSystemPath must end with a /");
			else if(!f.isDirectory())
				throw new IllegalArgumentException("file="+getCanonicalPath(f)+" is not a directory and must be for static directories");
			this.patternToMatch = Pattern.compile("^"+urlPath+"(?<resource>.*)$");
			this.pathParamNames.add("resource");
		} else {
			this.isFile = true;
			if(fileSystemPath.endsWith("/"))
				throw new IllegalArgumentException("Static file so fileSystemPath must NOT end with a /");
			else if(!f.isFile())
				throw new IllegalArgumentException("file="+getCanonicalPath(f)+" is not a file and must be for static file route");
			this.patternToMatch = Pattern.compile("^"+urlPath+"$");
		}
	}

	private String getCanonicalPath(File f) {
		try {
			return f.getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException(e);
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

	@Override
	public String toString() {
		return "StaticRoute [\n      urlPath=" + urlPath + ",\n      fileSystemPath=" + fileSystemPath + ",\n      isFile=" + isFile
				+ ",\n      patternToMatch=" + patternToMatch + ",\n      isOnClassPath=" + isOnClassPath + ",\n      pathParamNames="
				+ pathParamNames + "]";
	}

	public boolean isFile() {
		return isFile;
	}

	public int getStaticRouteId() {
		return this.uniqueStaticRouteId;
	}

}
