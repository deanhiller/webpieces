package org.webpieces.microsvc.server.api;

public class FilterConfig {
    private boolean entryPoint;
    private String packageRegEx;
    private boolean enableHealthCheckEndpoint = true;

    private boolean recordingEnabled = false;

    public FilterConfig(String packageRegEx) {
        this(packageRegEx, true);
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
}
