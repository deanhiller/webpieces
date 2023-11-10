package org.webpieces.util.context;

public interface PlatformHeaders {

    public String getHeaderName();

    public String getLoggerMDCKey();

    public boolean isWantLogged();

    public boolean isWantTransferred();

    public boolean isSecured();

    boolean isDimensionForMetrics();
}
