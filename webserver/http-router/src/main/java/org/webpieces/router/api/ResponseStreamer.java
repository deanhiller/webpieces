package org.webpieces.router.api;

import org.webpieces.router.api.dto.RedirectResponse;
import org.webpieces.router.api.dto.RenderResponse;

public interface ResponseStreamer {

	void sendRedirect(RedirectResponse httpResponse);
	
	void sendRenderHtml(RenderResponse resp);
	
	void failureRenderingInternalServerErrorPage(Throwable e);



}
