package org.webpieces.googlecloud.storage.impl;

import com.google.api.gax.paging.Page;
import org.webpieces.googlecloud.storage.api.GCPBlob;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LocalPage implements Page<GCPBlob> {

    private File directory;

    public LocalPage(File directory) {
        this.directory = directory;
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
        List<GCPBlob> blobList = new ArrayList<>();

        for(File f : directory.listFiles()) {
            blobList.add(new GCPBlobImpl(f));
        }

        return blobList;
    }

}
