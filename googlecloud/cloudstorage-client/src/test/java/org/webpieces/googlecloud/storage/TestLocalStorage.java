package org.webpieces.googlecloud.storage;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.googlecloud.storage.api.GCPBlob;
import org.webpieces.googlecloud.storage.api.GCPStorage;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class TestLocalStorage {

    private GCPStorage instance;

    @Before
    public void setup() {
        Module testModule = Modules.override(new FakeProdModule()).with(new LocalOverrideModule());

        Injector injector = Guice.createInjector(testModule);
        instance = injector.getInstance(GCPStorage.class);
    }

    @Test
    public void testReadFromClasspath() {
        ReadableByteChannel channel = instance.reader("testbucket", "mytest.txt");
        InputStream i = Channels.newInputStream(channel);

        String text = new BufferedReader(
                new InputStreamReader(i, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));

        Assert.assertEquals("Some Test", text);
    }

    @Test
    public void testWriteThenReadFromBuildDir() throws IOException {
        BlobId id = BlobId.of("testbucket", "fileShit.txt");
        BlobInfo info = BlobInfo.newBuilder(id).build();
        WritableByteChannel writer = instance.writer(info);
        OutputStream o = Channels.newOutputStream(writer);
        String fkingString = "testing a bitch";
        byte[] bytes = fkingString.getBytes(StandardCharsets.UTF_8);
        o.write(bytes);


        ReadableByteChannel channel = instance.reader("testbucket", "fileShit.txt");

        InputStream i = Channels.newInputStream(channel);

        String text = new BufferedReader(
                new InputStreamReader(i, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        Assert.assertEquals("testing a bitch", text);
    }

    @Test
    public void testListFilesFromBothResourcesDirAndBuildDir() {
        Page<GCPBlob> testbucket = instance.list("shitty");
        Iterable<GCPBlob> values = testbucket.getValues();
        for(GCPBlob shit : values) {
            Assert.fail();
        }
    }

    @Test
    public void validateFileNotFoundReturnsNullBlob() {

    }

    @Test
    public void addFileToBucketAndThenListFiles() {

    }

    @Test
    public void addFileThenReadThenDeleteThenListFiles() {

    }

    @Test
    public void testCopyFromClassPath() {

    }

    @Test
    public void testCopyFromBuildDirectory() {

    }
}
