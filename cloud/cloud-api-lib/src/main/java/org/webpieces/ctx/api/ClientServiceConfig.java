package org.webpieces.ctx.api;

import org.webpieces.microsvc.server.api.HeaderCtxList;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * configuration for clients and for service as they share all this information and both need it
 */
public class ClientServiceConfig {
    private HeaderCtxList hcl;
    private List<Class> successExceptions;
    private String serviceName;

    public ClientServiceConfig(HeaderCtxList hcl, String serviceName){
        this(hcl, null, serviceName);
    }
    public ClientServiceConfig(HeaderCtxList hcl, List<Class> successExceptions, String serviceName){
        this.hcl = hcl;
        this.successExceptions = successExceptions;
        this.serviceName = serviceName;
    }



    public String getServiceName() {
        return serviceName;
    }

    public HeaderCtxList getHcl() {
        return hcl;
    }

    public List<Class> getSuccessExceptions() {
        return successExceptions;
    }
}
