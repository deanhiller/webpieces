package org.webpieces.googlecloud.storage;

import java.net.URI;
import java.nio.channels.WritableByteChannel;
import java.util.List;

public interface GCPStorage {

    /**
     * @param bucket   Unprefixed bucket name
     * @param filename File path in bucket
     */
    BlobFile getBlobFile(String bucket, String filename);

    URI getFileInZip(String bucket, String filename);

    WritableByteChannel create(String bucket, String filename);

    WritableByteChannel create(String bucket, String filename, String mimeType);

    List<String> list(String bucket);

    void copy(BlobFile source, BlobFile destination);

    void copy(BlobFile source, String destBucket, String destName);

    boolean delete(BlobFile blobFile);

}
