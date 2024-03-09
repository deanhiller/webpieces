package org.webpieces.util.context;

public interface PlatformHeaders {

    /**
     * In some cases, header name is not transferred so isWantTransferred exists.  It is
     * for cases where we may store objects like a RequestContext everyone can access on
     * request path
     */
    public String getHeaderName();

    public String getLoggerMDCKey();

    /**
     * loggerMDC is null if not want logged
     * @deprecated
     */
    @Deprecated
    public boolean isWantLogged();

    public boolean isWantTransferred();

    public boolean isSecured();

    boolean isDimensionForMetrics();
}
