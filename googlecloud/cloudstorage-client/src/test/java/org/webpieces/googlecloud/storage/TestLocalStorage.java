package org.webpieces.googlecloud.storage;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.googlecloud.storage.api.CopyInterface;
import org.webpieces.googlecloud.storage.api.GCPBlob;
import org.webpieces.googlecloud.storage.api.GCPStorage;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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
    public void testListFilesFromBothResourcesDirAndBuildDir() throws IOException {
        //finish this test out

        Page<GCPBlob> testbucket = instance.list("listbucket");
        writeFile(BlobId.of("listbucket", "fileSystemFile1.txt"));
        writeFile(BlobId.of("listbucket", "fileSystemFile2.txt"));



        Iterable<GCPBlob> values = testbucket.getValues();
        Iterator<GCPBlob> iter = values.iterator();

        List<String> list = new ArrayList<String>();
        while(iter.hasNext()){
            list.add(iter.next().getName());
            }
            Collections.sort(list);


            Assert.assertEquals("fileSystemFile1.txt",list.get(0));
            Assert.assertEquals("fileSystemFile2.txt",list.get(1));
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
    public void addFileThenReadThenDeleteThenListFiles() throws IOException {
        writeFile(BlobId.of("testbucket", "mytest1.txt"));
        GCPBlob bucket = instance.get("testbucket", "mytest1.txt");
        Assert.assertEquals("mytest1.txt",bucket.getName());//passed.

        ReadableByteChannel readFile = instance.reader("testbucket", "mytest1.txt");

        InputStream i = Channels.newInputStream(readFile);

        String text = new BufferedReader(
                new InputStreamReader(i, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        Assert.assertEquals("testing a bitch", text);// Passed.

        boolean success = instance.delete("testbucket", "mytest1.txt");
        Assert.assertEquals(true,success);//passed.

        Page<GCPBlob> testbucket = instance.list("testbucket");
        Iterable<GCPBlob> values = testbucket.getValues();
        Iterator<GCPBlob> iter = values.iterator();
        List<String> list = new ArrayList<>();
        while(iter.hasNext()){
            list.add(iter.next().getName());
        }
        //length of the blob should be original length.
        Assert.assertEquals(2,list.size());// testing on testbucket directory
        // with 2 files already existed.
    }

    @Test
    public void testCopyFromClassPath() throws IOException {

        String bucketName = "testbucket";
        String blobName = "mytest.txt";
        String copyBlobName = "mytest_copy.txt";
        Storage.CopyRequest request = Storage.CopyRequest.newBuilder()
                .setSource(BlobId.of(bucketName, blobName))
                .setTarget(BlobId.of(bucketName, copyBlobName))
                .build();
        Page<GCPBlob> testbucket = instance.list("copybucket");
        CopyInterface copy = instance.copy(request);

        ReadableByteChannel readFile = instance.reader("copybucket", "mytest_copy.txt");

        InputStream i = Channels.newInputStream(readFile);

        String text = new BufferedReader(
                new InputStreamReader(i, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        Assert.assertEquals("Some Test", text);// Passed.
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
