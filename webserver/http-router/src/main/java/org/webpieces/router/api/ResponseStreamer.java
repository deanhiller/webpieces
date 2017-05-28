package org.webpieces.router.api;

import java.util.concurrent.CompletableFuture;

import org.webpieces.router.api.dto.RedirectResponse;
import org.webpieces.router.api.dto.RenderContentResponse;
import org.webpieces.router.api.dto.RenderResponse;
import org.webpieces.router.api.dto.RenderStaticResponse;

public interface ResponseStreamer {

	CompletableFuture<Void> sendRedirect(RedirectResponse httpResponse);
	
	CompletableFuture<Void> sendRenderHtml(RenderResponse resp);
	
	CompletableFuture<Void> sendRenderContent(RenderContentResponse resp);
	
	CompletableFuture<Void> sendRenderStatic(RenderStaticResponse renderStatic);

	CompletableFuture<Void> failureRenderingInternalServerErrorPage(Throwable e);

}
