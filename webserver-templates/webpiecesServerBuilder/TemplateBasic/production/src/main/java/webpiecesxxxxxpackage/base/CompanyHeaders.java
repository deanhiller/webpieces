package webpiecesxxxxxpackage.base;

import org.webpieces.util.context.PlatformHeaders;

public enum CompanyHeaders implements PlatformHeaders {

    CUSTOMER_ID("x-company-something", "logKey", true, false, false);

    private final String headerName;
    private final String logKey;
    private final boolean isLog;
    private final boolean isSecure;
    private final boolean isDimension;

    CompanyHeaders(String headerName, String logKey, boolean isLog, boolean isSecure, boolean isDimension) {
        this.headerName = headerName;
        this.logKey = logKey;
        this.isLog = isLog;
        this.isSecure = isSecure;
        this.isDimension = isDimension;
    }

    @Override
    public String getHeaderName() {
        return headerName;
    }

    @Override
    public String getLoggerMDCKey() {
        return logKey;
    }

    @Override
    public boolean isWantLogged() {
        return isLog;
    }

    @Override
    public boolean isWantTransferred() {
        return headerName != null;
    }

    @Override
    public boolean isSecured() {
        return isSecure;
    }

    @Override
    public boolean isDimensionForMetrics() {
        return isDimension;
    }
}
