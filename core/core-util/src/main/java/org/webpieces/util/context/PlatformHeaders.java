package org.webpieces.util.context;

public interface PlatformHeaders {

    String getHeaderName();

    String getLoggerMDCKey();

    boolean isWantLogged();

    boolean isWantTransferred();

}
