package org.webpieces.router.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.dto.RouteType;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.api.routing.Port;
import org.webpieces.router.impl.model.MatchResult;
import org.webpieces.util.file.FileFactory;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class StaticRoute implements Route {

	private static final Logger log = LoggerFactory.getLogger(StaticRoute.class);
	
	private String urlPath;
	private VirtualFile fileSystemPath;

	private boolean isFile = false;
	private Pattern patternToMatch;
	private boolean isOnClassPath;
	private List<String> pathParamNames = new ArrayList<>();
	private File targetCacheLocation;
	private Properties hashMeta;

	private Port exposedOnPorts;

	private RouteInvoker2 routeInvoker;

	public StaticRoute(RouteInvoker2 routeInvoker, Port port, UrlPath url, VirtualFile file, boolean isOnClassPath, File cachedCompressedDirectory) {
		this.routeInvoker = routeInvoker;
		this.fileSystemPath = file;
		this.isOnClassPath = isOnClassPath;
		this.exposedOnPorts = port;
		
		String urlSubPath = url.getSubPath();
		this.urlPath = url.getFullPath();

		//very big conflict between domain/path/path/path
		if(!urlSubPath.startsWith("/"))
			throw new IllegalArgumentException("static resource url paths must start with / and can't have domain name at this time="+urlSubPath);

		if(!file.exists())
			throw new IllegalArgumentException("Static File="+file.getCanonicalPath()+" does not exist. fileSysPath="+this.fileSystemPath+" abs="+file.getAbsolutePath());

		if(isDirectory(urlSubPath)) {
			this.isFile = false;
			if(!file.isDirectory())
				throw new IllegalArgumentException("Static directory so fileSystemPath must end with a /");
			else if(!file.isDirectory())
				throw new IllegalArgumentException("file="+file.getCanonicalPath()+" is not a directory and must be for static directories");
			this.patternToMatch = Pattern.compile("^"+urlSubPath+"(?<resource>.*)$");
			this.pathParamNames.add("resource");
		} else {
			this.isFile = true;
			if(file.isDirectory())
				throw new IllegalArgumentException("Static file so fileSystemPath must NOT end with a /");
			else if(!file.isFile())
				throw new IllegalArgumentException("file="+file.getCanonicalPath()+" is not a file and must be for static file route");
			this.patternToMatch = Pattern.compile("^"+urlSubPath+"$");
		}
		
		String relativePath = urlSubPath.substring(1);
		this.targetCacheLocation = FileFactory.newFile(cachedCompressedDirectory, relativePath);
	}

	private boolean isDirectory(String urlSubPath) {
		return urlSubPath.endsWith("/");
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

	public VirtualFile getFileSystemPath() {
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
	public Port getExposedPorts() {
		return exposedOnPorts;
	}

	public void setHashMeta(Properties hashMeta) {
		this.hashMeta = hashMeta;
	}

	@Override
	public String getMethod() {
		return "GET";
	}

	@Override
	public HttpMethod getHttpMethod() {
		return HttpMethod.GET;
	}

	@Override
	public CompletableFuture<Void> invokeImpl(MatchResult result, RequestContext ctx, ResponseStreamer responseCb, NotFoundException exc) {
		return routeInvoker.invokeStatic(result, ctx, responseCb);
	}

	@Override
	public List<String> getArgNames() {
		return null;
	}

}
