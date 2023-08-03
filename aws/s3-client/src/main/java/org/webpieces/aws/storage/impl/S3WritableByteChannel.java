package org.webpieces.aws.storage.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;

public class S3WritableByteChannel implements WritableByteChannel {

    private final S3Client client;
    private final String bucket;
    private final String key;
    private final ByteBuffer bb;

    private boolean closed = false;
    private int part = 1;

    public S3WritableByteChannel(final S3Client client, final String bucket, final String key) {

        this.client = client;
        this.bucket = bucket;
        this.key = key;

        this.bb = ByteBuffer.allocate(1024 * 1024 * 8);

    }

    @Override
    public int write(ByteBuffer src) throws IOException {

        int bytesWritten = 0;

        while(src.remaining() > 0) {

            if ((src.remaining()) <= (bb.remaining())) {
                bytesWritten += (src.remaining());
                bb.put(src);
            } else {

                ByteBuffer tmp = src.duplicate();
                tmp.limit(bb.remaining());

                bytesWritten += tmp.remaining();
                bb.put(tmp);

            }

            if (!bb.hasRemaining()) {
                bb.flip();
                uploadPart();
            }

        }

        return bytesWritten;

    }

    @Override
    public boolean isOpen() {
        return closed;
    }

    @Override
    public void close() throws IOException {

        if(closed) {
            return;
        }

        if(bb.position() > 0) {
            bb.flip();
            uploadPart();
        }

        closed = true;

    }

    private void uploadPart() {

        UploadPartRequest request = UploadPartRequest.builder()
                .bucket(bucket)
                .key(key)
                .partNumber(part)
                .build();

        client.uploadPart(request, RequestBody.fromRemainingByteBuffer(bb));

        part++;
        bb.clear();

    }

}
