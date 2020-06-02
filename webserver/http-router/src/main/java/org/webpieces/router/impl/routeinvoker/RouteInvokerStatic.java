package org.webpieces.router.impl.routeinvoker;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.data.api.BufferPool;
import org.webpieces.router.impl.dto.RenderStaticResponse;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.proxyout.filereaders.StaticFileReader;
import org.webpieces.router.impl.routers.NullStreamWriter;
import org.webpieces.router.impl.services.RouteInfoForStatic;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.futures.FutureHelper;

import com.webpieces.http2engine.api.StreamRef;
import com.webpieces.http2engine.api.StreamWriter;

public class RouteInvokerStatic {

	private StaticFileReader reader;
	private BufferPool pool;
	protected FutureHelper futureUtil;
	
	@Inject
	public RouteInvokerStatic(
			StaticFileReader reader, 
			BufferPool pool, 
			FutureHelper futureUtil
	) {
		super();
		this.reader = reader;
		this.pool = pool;
		this.futureUtil = futureUtil;
	}


	public RouterStreamRef invokeStatic(RequestContext ctx, ProxyStreamHandle handler, RouteInfoForStatic data) {
		
		boolean isOnClassPath = data.isOnClassPath();

		RenderStaticResponse resp = new RenderStaticResponse(data.getTargetCacheLocation(), isOnClassPath);

		//NOTE: Looking up resource pictures in localhost:8080/@documentation stopped working if we
		//did not use the data.isRouteAFile() and used the filesystem information
		//we do have a test for this now if you try to fix it
		if(data.isRouteAFile()) {
			resp.setFilePath(data.getFileSystemPath());
		} else {
			String relativeUrl = ctx.getPathParams().get("resource");
			VirtualFile fullPath = data.getFileSystemPath().child(relativeUrl);
			resp.setFileAndRelativePath(fullPath, relativeUrl);
		}

		ResponseStaticProcessor processor = new ResponseStaticProcessor(reader, pool, futureUtil, ctx, handler);

		return processor.renderStaticResponse(resp);
	}
}
