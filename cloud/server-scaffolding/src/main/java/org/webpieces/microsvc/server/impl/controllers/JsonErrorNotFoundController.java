package org.webpieces.microsvc.server.impl.controllers;

import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import org.webpieces.http.StatusCode;
import org.webpieces.router.api.controller.actions.Render;
import org.webpieces.router.api.controller.actions.RenderContent;
import org.webpieces.router.impl.compression.MimeTypes;

import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;

@Singleton
public class JsonErrorNotFoundController {

    private static final MimeTypes.MimeTypeResult MIME_TYPE = new MimeTypes.MimeTypeResult("application/json", StandardCharsets.UTF_8);

    public Render notFound() {
        return new RenderContent(new byte[0], StatusCode.HTTP_404_NOT_FOUND.getCode(), StatusCode.HTTP_404_NOT_FOUND.getReason(), MIME_TYPE);
    }

    public Render internalError() {
        return new RenderContent(new byte[0], StatusCode.HTTP_500_INTERNAL_SERVER_ERROR.getCode(), StatusCode.HTTP_500_INTERNAL_SERVER_ERROR.getReason(), MIME_TYPE);
    }

}
