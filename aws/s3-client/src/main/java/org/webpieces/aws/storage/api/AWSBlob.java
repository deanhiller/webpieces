package org.webpieces.aws.storage.api;

public interface AWSBlob {

    String getBucket();

    String getKey();

    String getContentType();

    long getSize();

}