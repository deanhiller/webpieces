package org.webpieces.router.api;

import java.util.concurrent.CompletableFuture;

import org.webpieces.router.impl.dto.RedirectResponse;
import org.webpieces.router.impl.dto.RenderContentResponse;
import org.webpieces.router.impl.dto.RenderResponse;
import org.webpieces.router.impl.dto.RenderStaticResponse;

public interface ResponseStreamer {

	CompletableFuture<Void> sendRedirect(RedirectResponse httpResponse);
	
	CompletableFuture<Void> sendRenderHtml(RenderResponse resp);
	
	CompletableFuture<Void> sendRenderContent(RenderContentResponse resp);
	
	CompletableFuture<Void> sendRenderStatic(RenderStaticResponse renderStatic);

	CompletableFuture<Void> failureRenderingInternalServerErrorPage(Throwable e);

}
