package org.webpieces.googlecloud.storage.impl.local;

import org.webpieces.googlecloud.storage.api.GCPBlob;

public class LocalAWSBlobImpl implements GCPBlob {
    String bucket;
    String name;
    String contentType;
    long size;

    public LocalAWSBlobImpl(String bucket, String name) {
        this.bucket = bucket;
        this.name = name;
    }

    @Override
    public String getBucket() {
        return bucket;
    }

    @Override
    public String getName() {
        return name;
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
