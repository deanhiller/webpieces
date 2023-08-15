package org.webpieces.ctx.api;

import org.webpieces.microsvc.server.api.HeaderCtxList;

import java.util.List;

/**
 * configuration for clients and for service as they share all this information and both need it
 */
public class ClientServiceConfig {
    private HeaderCtxList hcl;
    private List<Class> successExceptions;
    private String serversName;

    public ClientServiceConfig(HeaderCtxList hcl, String serversName){
        this(hcl, null, serversName);
    }
    public ClientServiceConfig(HeaderCtxList hcl, List<Class> successExceptions, String serviceName){
        this.hcl = hcl;
        this.successExceptions = successExceptions;
        this.serversName = serviceName;
    }



    public String getServersName() {
        return serversName;
    }

    public HeaderCtxList getHcl() {
        return hcl;
    }

    public List<Class> getSuccessExceptions() {
        return successExceptions;
    }
}
