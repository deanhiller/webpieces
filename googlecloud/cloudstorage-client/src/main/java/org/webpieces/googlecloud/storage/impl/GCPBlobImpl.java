package org.webpieces.googlecloud.storage.impl;

import org.webpieces.googlecloud.storage.api.GCPBlob;

import java.io.File;

public class GCPBlobImpl implements GCPBlob {
    private File localFile;

    public GCPBlobImpl(File localFile) {
        this.localFile = localFile;
    }

}
