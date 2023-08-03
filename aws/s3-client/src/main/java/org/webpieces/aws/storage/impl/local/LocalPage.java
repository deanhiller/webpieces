package org.webpieces.googlecloud.storage.impl.local;

import com.google.api.gax.paging.Page;
import org.webpieces.googlecloud.storage.api.GCPBlob;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LocalPage implements Page<GCPBlob> {

    private String bucket;
    private File directory;

    public LocalPage(String bucket, File directory) {
        this.bucket = bucket;
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
        return getValues();
    }

    @Override
    public Iterable<GCPBlob> getValues() {
        List<GCPBlob> blobList = new ArrayList<>();

        for(File f : directory.listFiles()) {
            blobList.add(new LocalAWSBlobImpl(bucket, f.getName()));
        }

        return blobList;
    }

}
