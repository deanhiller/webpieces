package org.webpieces.aws.storage.impl.local;

import org.webpieces.aws.storage.api.AWSBlob;

public class LocalAWSBlobImpl implements AWSBlob {
    String bucket;
    String key;
    String contentType;
    long size;

    public LocalAWSBlobImpl(String bucket, String key, String contentType, long size) {
        this.bucket = bucket;
        this.key = key;
        this.contentType = contentType;
        this.size = size;
    }

    @Override
    public String getBucket() {
        return bucket;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public long getSize() {
        return size;
    }
}
