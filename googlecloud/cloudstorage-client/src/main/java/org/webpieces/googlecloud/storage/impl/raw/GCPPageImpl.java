package org.webpieces.googlecloud.storage.impl.raw;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import org.webpieces.googlecloud.storage.api.GCPBlob;

public class GCPPageImpl implements Page<GCPBlob> {

    private Page<Blob> list;

    public GCPPageImpl(Page<Blob> list) {
        this.list = list;
    }

    @Override
    public boolean hasNextPage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getNextPageToken() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Page<GCPBlob> getNextPage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<GCPBlob> iterateAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<GCPBlob> getValues() {
        throw new UnsupportedOperationException();
    }
}
