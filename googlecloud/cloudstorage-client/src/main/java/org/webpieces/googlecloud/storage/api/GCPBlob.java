package org.webpieces.googlecloud.storage.api;

public interface GCPBlob {

    String getBucket();

    String getName();

    String getContentType();

    long getSize();




}