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
        if(hcl == null) {
            throw new IllegalArgumentException("Pass in a non-null hcl param please.  We need the class still to determine which jar the application is in." +
                    " This will be deprecated in the future");
        }
        this.hcl = hcl;
        if(hcl.listHeaderCtxPairs() != null) {
            throw new IllegalArgumentException("listHeaderCtxPairs() must return null AND add this instead -> \n" +
                        "Multibinder<AddPlatformHeaders> htmlTagCreators = Multibinder.newSetBinder(binder, AddPlatformHeaders.class);\n" +
                        "htmlTagCreators.addBinding().to(CompanyHeaders.class);\n\n" +
                        "We now use binders instead so plugins can add headers they need");
        }
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
