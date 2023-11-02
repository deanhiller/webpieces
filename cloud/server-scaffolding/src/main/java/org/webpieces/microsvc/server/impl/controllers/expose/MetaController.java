package org.webpieces.microsvc.server.impl.controllers.expose;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.ClientServiceConfig;
import org.webpieces.microsvc.server.api.HeaderCtxList;
import org.webpieces.plugin.json.Jackson;
import org.webpieces.router.impl.compression.MimeTypes;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;

@Singleton
public class MetaController {

    private static final Logger log = LoggerFactory.getLogger(MetaController.class);
    private static final MimeTypes.MimeTypeResult MIME_TYPE = new MimeTypes.MimeTypeResult("application/json", StandardCharsets.UTF_8);

    private VersionResponse response = new VersionResponse();
    private HealthResponse healthResponse = new HealthResponse();

    @Inject
    public MetaController(ClientServiceConfig config) {
        HeaderCtxList classFromClientJar = config.getHcl();
        response.setImplementationVersion("development");
        try{
            String version = classFromClientJar.getClass().getPackage().getImplementationVersion();
            if(version != null) {
                response.setImplementationVersion(version);
            }
        }catch(Exception e) {
            log.error("Error occurred during version retrieval", e);
            throw e;
        }
    }

    @Jackson
    public HealthResponse health() {
        return healthResponse;
    }

    @Jackson
    public VersionResponse version() {
        return response;
    }
}
