package org.webpieces.router.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
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
	private File targetCacheLocation;
	private Properties hashMeta;

	public StaticRoute(UrlPath url, String fileSystemPath, boolean isOnClassPath, File cachedCompressedDirectory) {
		this.fileSystemPath = fileSystemPath;
		this.isOnClassPath = isOnClassPath;
		
		String urlSubPath = url.getSubPath();
		this.urlPath = url.getFullPath();

		//very big conflict between domain/path/path/path
		if(!urlSubPath.startsWith("/"))
			throw new IllegalArgumentException("static resource url paths must start with / and can't have domain name at this time="+urlSubPath);
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

		if(isDirectory(urlSubPath)) {
			this.isFile = false;
			if(!isDirectory(fileSystemPath))
				throw new IllegalArgumentException("Static directory so fileSystemPath must end with a /");
			else if(!f.isDirectory())
				throw new IllegalArgumentException("file="+getCanonicalPath(f)+" is not a directory and must be for static directories");
			this.patternToMatch = Pattern.compile("^"+urlSubPath+"(?<resource>.*)$");
			this.pathParamNames.add("resource");
		} else {
			this.isFile = true;
			if(isDirectory(fileSystemPath))
				throw new IllegalArgumentException("Static file so fileSystemPath must NOT end with a /");
			else if(!f.isFile())
				throw new IllegalArgumentException("file="+getCanonicalPath(f)+" is not a file and must be for static file route");
			this.patternToMatch = Pattern.compile("^"+urlSubPath+"$");
		}
		
		String relativePath = urlSubPath.substring(1);
		this.targetCacheLocation = new File(cachedCompressedDirectory, relativePath);
	}

	private boolean isDirectory(String urlSubPath) {
		return urlSubPath.endsWith("/");
	}

	private String getCanonicalPath(File f) {
		try {
			return f.getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getFullPath() {
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
		
		String hash = null;
		List<String> list = request.queryParams.get("hash");
		if(list != null && list.size() > 0)
			hash = list.get(0);
		
		if(hashMeta != null) {
			//MUST be in production mode if we are here as only production sets hashMeta
			
			if(matcher.matches() && hash != null) {
				//DO NOT allow a browser to cache a file not matching the hash because that is unfixable once 
				//customers have that file until it expires and generally all expires should be infinite since
				//we hash every file for you so browsers always load latest
				String filesHash = hashMeta.getProperty(request.relativePath);
				if(!hash.equals(filesHash))
					return null;
			}
		}
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
		return "\nStaticRoute [\n      urlPath=" + urlPath + ",\n      fileSystemPath=" + fileSystemPath + ",\n      isFile=" + isFile
				+ ",\n      patternToMatch=" + patternToMatch + ",\n      isOnClassPath=" + isOnClassPath + ",\n      pathParamNames="
				+ pathParamNames + "]";
	}

	public boolean isFile() {
		return isFile;
	}

	public File getTargetCacheLocation() {
		return this.targetCacheLocation;
	}

	@Override
	public boolean isHttpsRoute() {
		throw new UnsupportedOperationException("This method is not necessary as there are no filters for static routes at this time");
	}

	public void setHashMeta(Properties hashMeta) {
		this.hashMeta = hashMeta;
	}

	@Override
	public String getMethod() {
		return "GET";
	}

}
