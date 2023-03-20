package org.webpieces.microsvc.server.api;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class FilterConfig {
    private boolean entryPoint;
    private String packageRegEx;
    private HeaderCtxList headers;
    private Supplier<String> lazySvcName;
    private boolean enableHealthCheckEndpoint = true;

    public FilterConfig(String packageRegEx, HeaderCtxList headers, Supplier<String> lazySvcName) {
        this(packageRegEx, headers, lazySvcName, true);
    }

    public FilterConfig(String packageRegEx, HeaderCtxList headers, Supplier<String> lazySvcName, boolean entryPoint) {
        this.packageRegEx = packageRegEx;
        this.headers = headers;
        this.lazySvcName = lazySvcName;
        this.entryPoint = entryPoint;
    }

    public String getPackageRegEx() {
        return packageRegEx;
    }

    public void setPackageRegEx(String packageRegEx) {
        this.packageRegEx = packageRegEx;
    }

    public Supplier<String> getSvcName() {
        return lazySvcName;
    }

    public void setLazySvcName(Supplier<String> lazySvcName) {
        this.lazySvcName = lazySvcName;
    }

    public boolean isEnableHealthCheckEndpoint() {
        return enableHealthCheckEndpoint;
    }

    public void setEnableHealthCheckEndpoint(boolean enableHealthCheckEndpoint) {
        this.enableHealthCheckEndpoint = enableHealthCheckEndpoint;
    }

    public HeaderCtxList getHeaders() {
        return headers;
    }

    public void setHeaders(HeaderCtxList headers) {
        this.headers = headers;
    }

    public boolean isEntryPoint() {
        return entryPoint;
    }

    public void setEntryPoint(boolean entryPoint) {
        this.entryPoint = entryPoint;
    }
}
