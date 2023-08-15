package org.webpieces.aws.storage.impl;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.Serializable;
import java.util.function.Supplier;

public class StorageSupplier implements Supplier<S3Client>, Serializable {

    @Override
    public S3Client get() {
        return S3Client.builder().region(Region.US_WEST_2).build();
    }

}
