package org.webpieces.microsvc.server.impl;

import org.webpieces.recorder.impl.EndpointInfo;

public class MetaVarInfo {
    private String svcVarName;
    private String svcName;
    private String requestClassName;
    private String responseBeanClassName;
    private final EndpointInfo info;

    public MetaVarInfo(String svcVarName, String svcName, String requestClassName, String responseBeanClassName, EndpointInfo info) {

        this.svcVarName = svcVarName;
        this.svcName = svcName;
        this.requestClassName = requestClassName;
        this.responseBeanClassName = responseBeanClassName;
        this.info = info;
    }

    public String getSvcVarName() {
        return svcVarName;
    }

    public EndpointInfo getInfo() {
        return info;
    }

    public String getRequestClassName() {
        return requestClassName;
    }

    public String getSvcName() {
        return svcName;
    }

    public String getResponseBeanClassName() {
        return responseBeanClassName;
    }
}
