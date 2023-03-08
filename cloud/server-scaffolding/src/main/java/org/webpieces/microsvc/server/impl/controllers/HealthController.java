package org.webpieces.microsvc.server.impl.controllers;

import org.webpieces.plugin.json.Jackson;
import org.webpieces.router.impl.compression.MimeTypes;

import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;

@Singleton
public class HealthController {

    private static final MimeTypes.MimeTypeResult MIME_TYPE = new MimeTypes.MimeTypeResult("application/json", StandardCharsets.UTF_8);

    @Jackson
    public HealthResponse health() {
        return new HealthResponse();
    }

}
