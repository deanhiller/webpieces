package org.webpieces.router.api;

import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.impl.dto.RenderContentResponse;
import org.webpieces.router.impl.dto.RenderResponse;
import org.webpieces.router.impl.dto.RenderStaticResponse;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;

public interface ResponseStreamer {

	CompletableFuture<Void> sendRenderHtml(RenderResponse resp);

	CompletableFuture<Void> sendRenderContent(RenderContentResponse resp);

	CompletableFuture<Void> sendRenderStatic(RenderStaticResponse renderStatic, ProxyStreamHandle handle);

	void init(RouterRequest request, ProxyStreamHandle handler);
}
