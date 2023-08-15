package org.webpieces.aws.storage.impl.raw;

import org.webpieces.aws.storage.api.AWSBlob;

public class AWSBlobImpl implements AWSBlob {

    private String bucket;
    private String key;
    private String contentType;
    private long size;

    public AWSBlobImpl(String bucket, String key, String contentType, long size) {
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
