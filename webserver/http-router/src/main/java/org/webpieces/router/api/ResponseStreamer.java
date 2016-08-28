package org.webpieces.router.api;

import java.util.concurrent.CompletableFuture;

import org.webpieces.router.api.dto.RedirectResponse;
import org.webpieces.router.api.dto.RenderResponse;
import org.webpieces.router.api.dto.RenderStaticResponse;

public interface ResponseStreamer {

	void sendRedirect(RedirectResponse httpResponse);
	
	void sendRenderHtml(RenderResponse resp);
	
	CompletableFuture<Void> sendRenderStatic(RenderStaticResponse renderStatic);

	void failureRenderingInternalServerErrorPage(Throwable e);

}
