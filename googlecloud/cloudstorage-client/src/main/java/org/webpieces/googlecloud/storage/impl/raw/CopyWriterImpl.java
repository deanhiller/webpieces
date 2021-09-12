package org.webpieces.googlecloud.storage.impl.raw;

import com.google.cloud.storage.CopyWriter;
import org.webpieces.googlecloud.storage.api.CopyInterface;

public class CopyWriterImpl implements CopyInterface {
    private CopyWriter copyWriter;


    public CopyWriterImpl(CopyWriter copyWriter) {

        this.copyWriter = copyWriter;
    }
}
