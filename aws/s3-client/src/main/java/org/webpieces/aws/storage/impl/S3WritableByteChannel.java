package org.webpieces.aws.storage.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

public class S3WritableByteChannel implements WritableByteChannel {

    private final S3Client client;
    private final String bucket;
    private final String key;
    private final ByteBuffer bb;

    private boolean closed = false;
    private int part = 1;
    private String uploadId;
    private List<CompletedPart> parts = new ArrayList<>();

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

        if(part > 1) {
            CompleteMultipartUploadRequest req = CompleteMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(key)
                .uploadId(uploadId)
                .multipartUpload((b) -> b.parts(parts))
                .build();
            client.completeMultipartUpload(req);
        }

    }

    private void uploadPart() {

        if(part == 1) {
            CreateMultipartUploadRequest req = CreateMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
            uploadId = client.createMultipartUpload(req).uploadId();
        }

        UploadPartRequest request = UploadPartRequest.builder()
                .bucket(bucket)
                .key(key)
                .partNumber(part)
                .build();

        UploadPartResponse resp = client.uploadPart(request, RequestBody.fromRemainingByteBuffer(bb));
        CompletedPart p = CompletedPart.builder().partNumber(part).eTag(resp.eTag()).build();
        parts.add(p);

        part++;
        bb.clear();

    }

}
