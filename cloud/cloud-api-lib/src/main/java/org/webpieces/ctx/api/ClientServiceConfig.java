package org.webpieces.ctx.api;

import org.webpieces.microsvc.server.api.HeaderCtxList;

import javax.inject.Inject;
import java.util.function.Supplier;

public class ClientServiceConfig {
    private HeaderCtxList hcl;
    private String serviceName;

    public ClientServiceConfig(HeaderCtxList hcl, String serviceName){
        this.hcl = hcl;
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public HeaderCtxList getHcl() {
        return hcl;
    }

}
