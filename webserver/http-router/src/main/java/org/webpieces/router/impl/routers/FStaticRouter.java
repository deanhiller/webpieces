package org.webpieces.router.impl.routers;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routeinvoker.RouteInvoker;
import org.webpieces.router.impl.routeinvoker.RouterStreamRef;
import org.webpieces.router.impl.services.RouteInfoForStatic;
import org.webpieces.util.file.VirtualFile;

public class FStaticRouter extends AbstractRouterImpl {

	private final RouteInvoker invoker;
	private final boolean isOnClassPath;
	private final File targetCacheLocation;
	private boolean isFile;
	private final VirtualFile fileSystemPath;
	private Properties hashMeta;

	public FStaticRouter(RouteInvoker invoker, MatchInfo matchInfo, VirtualFile fileSystemPath, boolean isOnClassPath, File targetCatchLocation, boolean isFile) {
		super(matchInfo);
		this.invoker = invoker;
		this.fileSystemPath = fileSystemPath;
		this.isOnClassPath = isOnClassPath;
		this.targetCacheLocation = targetCatchLocation;


		this.isFile = isFile;
	}

	@Override
	public RouterStreamRef invoke(RequestContext ctx, ProxyStreamHandle handler) {
		RouteInfoForStatic routeInfo = new RouteInfoForStatic(isOnClassPath, targetCacheLocation, fileSystemPath, isFile);
		return invoker.invokeStatic(ctx, handler, routeInfo);
	}

	public boolean isOnClassPath() {
		return isOnClassPath;
	}

	public File getTargetCacheLocation() {
		return targetCacheLocation;
	}

	public VirtualFile getFileSystemPath() {
		return fileSystemPath;
	}

	public String getFullPath() {
		return matchInfo.getFullPath();
	}

	public void setHashMeta(Properties properties) {
		this.hashMeta = properties;
	}

	@Override
	public Matcher matchesAndParseParams(RouterRequest request, String subPath) {
		if(!matchInfo.methodMatches(request.method)) {
			return null;
		}

		Matcher matcher = matchInfo.getPattern().matcher(subPath);
		
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

}
