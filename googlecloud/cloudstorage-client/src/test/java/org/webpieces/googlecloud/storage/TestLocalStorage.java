package org.webpieces.googlecloud.storage;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
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
        writeFile(id);


        ReadableByteChannel channel = instance.reader("testbucket", "fileShit.txt");

        InputStream i = Channels.newInputStream(channel);

        String text = new BufferedReader(
                new InputStreamReader(i, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        Assert.assertEquals("testing a bitch", text);
    }

    private void writeFile(BlobId id) throws IOException {
        BlobInfo info = BlobInfo.newBuilder(id).build();
        WritableByteChannel writer = instance.writer(info);
        OutputStream o = Channels.newOutputStream(writer);
        String fkingString = "testing a bitch";
        byte[] bytes = fkingString.getBytes(StandardCharsets.UTF_8);
        o.write(bytes);
        o.flush();
        o.close();
    }

    @Test
    public void testListFilesFromBothResourcesDirAndBuildDir() {
        //finish this test out

        Page<GCPBlob> testbucket = instance.list("shitty");
        Iterable<GCPBlob> values = testbucket.getValues();
        for(GCPBlob shit : values) {
            //
        }
    }

    @Test
    public void validateFileNotFoundReturnsNullBlob() {
        GCPBlob basket = instance.get("backet", "non-existent");
        Assert.assertNull(basket);
    }

    @Test
    public void testGetBlobClassPath() {
        GCPBlob testbucket = instance.get("testbucket", "mytest.txt");
        Assert.assertEquals("mytest.txt",testbucket.getName());
    }
    
    @Test
    public void testGetBlobFileSystem() throws IOException {
        //create a file
        BlobId id = BlobId.of("testbucket", "fileSystemFile.txt");
        writeFile(id);


        GCPBlob bucket = instance.get("testbucket", "fileSystemFile.txt");
        Assert.assertEquals("fileSystemFile.txt",bucket.getName());
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

    @Test
    public void testGetBUcket() {
    }

    @Test
    public void testAllCallsFailInTransaction() {

    }

    @Test
    public void testNoReadingWhileInTransaction() {

    }

}
