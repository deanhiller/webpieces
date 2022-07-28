package org.webpieces.plugin.json;

import javax.inject.Singleton;

import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.router.api.controller.actions.Render;
import org.webpieces.router.api.controller.actions.RenderContent;

@Singleton
public class JacksonDefaultErrorController {

    public Render internalError() {
        return new RenderContent(new byte[0], KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR.getCode(), KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR.getReason(), JacksonCatchAllFilter.MIME_TYPE);
    }

    public Render notFound() {
        return new RenderContent(new byte[0], KnownStatusCode.HTTP_404_NOTFOUND.getCode(), KnownStatusCode.HTTP_404_NOTFOUND.getReason(), JacksonCatchAllFilter.MIME_TYPE);
    }

}
