package org.webpieces.googlecloud.storage.impl.raw;

import com.google.cloud.storage.Blob;
import org.webpieces.googlecloud.storage.api.GCPBlob;

import java.io.File;

public class GCPBlobImpl implements GCPBlob {

    private Blob blob;

    public GCPBlobImpl(Blob blob) {
        this.blob = blob;
    }

}
