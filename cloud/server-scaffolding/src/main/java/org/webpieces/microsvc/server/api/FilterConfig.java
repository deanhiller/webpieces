package org.webpieces.microsvc.server.api;

public class FilterConfig {
    private boolean entryPoint;
    private String packageRegEx;
    private boolean enableHealthCheckEndpoint = true;
    private boolean secure;

    //Turning this off is an EXTREMELY minor performance improvement so default to on because only
    //requests with MicroSvcHeaders.RECORDING header will actually do recording
    private boolean recordingEnabled = true;

    private boolean enableErrorHandling = true;

    public FilterConfig(String packageRegEx) {
        this(packageRegEx, true);
    }

    public FilterConfig(boolean secure, String packageRegEx) {
        this.packageRegEx = packageRegEx;
        this.secure = secure;
    }

    public FilterConfig(String packageRegEx, boolean entryPoint) {
        this.packageRegEx = packageRegEx;
        this.entryPoint = entryPoint;
    }

    public String getPackageRegEx() {
        return packageRegEx;
    }

    public void setPackageRegEx(String packageRegEx) {
        this.packageRegEx = packageRegEx;
    }

    public FilterConfig setEnableErrorHandling(boolean enable) {
        this.enableErrorHandling = enable;
        return this;
    }

    public boolean isEnableErrorHandling() {
        return enableErrorHandling;
    }

    public boolean isEnableHealthCheckEndpoint() {
        return enableHealthCheckEndpoint;
    }

    public void setEnableHealthCheckEndpoint(boolean enableHealthCheckEndpoint) {
        this.enableHealthCheckEndpoint = enableHealthCheckEndpoint;
    }

    public boolean isEntryPoint() {
        return entryPoint;
    }

    public void setEntryPoint(boolean entryPoint) {
        this.entryPoint = entryPoint;
    }

    public boolean isRecordingEnabled() {
        return recordingEnabled;
    }

    public void setRecordingEnabled(boolean recordingEnabled) {
        this.recordingEnabled = recordingEnabled;
    }

    public boolean isSecure() { return secure; }

    public void setSecure(boolean secure) { this.secure = secure; }

}
