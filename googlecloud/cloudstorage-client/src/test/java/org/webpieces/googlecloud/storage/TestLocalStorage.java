package org.webpieces.googlecloud.storage;

import com.google.api.client.util.Lists;
import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.googlecloud.storage.api.CopyInterface;
import org.webpieces.googlecloud.storage.api.GCPBlob;
import org.webpieces.googlecloud.storage.api.GCPStorage;
import org.webpieces.util.context.Context;

import java.io.*;
import java.nio.ByteBuffer;
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
    @After
    public void tearDown() {
        List<String> buckets = new ArrayList<String>();
        buckets.add("testbucket");

        for(String bucket : buckets) {
            deleteFilesInBucket(bucket);
        }
    }

    private void deleteFilesInBucket(String bucket) {
        Page<GCPBlob> list = instance.list(bucket);
        for(GCPBlob blob : list.iterateAll()) {
            deleteFile(blob);
        }
    }

    private void deleteFile(GCPBlob blob) {
        instance.delete(blob.getBucket(), blob.getName());
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
    public void addFileToBucketAndThenListFiles() throws IOException {
        instance.list("ListFilebucket");
        writeFile(BlobId.of("ListFilebucket", "mytest1.txt"));
        GCPBlob bucket = instance.get("ListFilebucket", "mytest1.txt");
        Assert.assertEquals("mytest1.txt",bucket.getName());//passed.

        ReadableByteChannel readFile = instance.reader("ListFilebucket", "mytest1.txt");

        InputStream i = Channels.newInputStream(readFile);

        String text = new BufferedReader(
                new InputStreamReader(i, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        Page<GCPBlob> listfilebucket = instance.list("ListFilebucket");
        Iterable<GCPBlob> values = listfilebucket.getValues();
        Iterator<GCPBlob> iter = values.iterator();
        List<String> list = new ArrayList<>();
        while(iter.hasNext()){
            list.add(iter.next().getName());
        }
        Assert.assertEquals(1,list.size());

    }

    @Test
    public void addFileThenReadThenDeleteThenListFiles() throws IOException {
        writeFile(BlobId.of("AddReadDeleteListbucket", "mytest1.txt"));
        GCPBlob bucket = instance.get("AddReadDeleteListbucket", "mytest1.txt");
        Assert.assertEquals("mytest1.txt",bucket.getName());//passed.

        ReadableByteChannel readFile = instance.reader("AddReadDeleteListbucket", "mytest1.txt");

        InputStream i = Channels.newInputStream(readFile);

        String text = new BufferedReader(
                new InputStreamReader(i, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        Assert.assertEquals("testing a bitch", text);// Passed.

        boolean success = instance.delete("AddReadDeleteListbucket", "mytest1.txt");
        Assert.assertEquals(true,success);//passed.

        Page<GCPBlob> testbucket = instance.list("AddReadDeleteListbucket");
        Iterable<GCPBlob> values = testbucket.getValues();
        Iterator<GCPBlob> iter = values.iterator();
        List<String> list = new ArrayList<>();
        while(iter.hasNext()){
            list.add(iter.next().getName());
        }
        //length of the blob should be original length.
        Assert.assertEquals(0,list.size());// testing on testbucket directory
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

//    @Test
//    public void testCopyFromBuildDirectory() throws IOException {
//        // which build directory?
//        //step 1: write file to
//        //step 2: copy file to the same bucket with different file name.
//        //step 3: read it in and make sure it exists as a copy.
//        String str = "Hello";
//        instance.list("build-dir-copybucket");
//        File file = new File("build/local-cloudstorage/build-dir-copybucket/originalfile.txt");
//        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
//        writer.write(str);
//        writer.close();
//        String bucketName = "build-dir-copybucket";
//        String blobName = "originalfile.txt";
//        String copyBlobName = "originalfile_copy.txt";
//        Storage.CopyRequest request = Storage.CopyRequest.newBuilder()
//                .setSource(BlobId.of(bucketName, blobName))
//                .setTarget(BlobId.of(bucketName, copyBlobName))
//                .build();
//        instance.copy(request);
//
//        ReadableByteChannel readFile = instance.reader("copybucket", copyBlobName);
//        InputStream i = Channels.newInputStream(readFile);
//
//        String text = new BufferedReader(
//                new InputStreamReader(i, StandardCharsets.UTF_8))
//                .lines()
//                .collect(Collectors.joining("\n"));
//        Assert.assertEquals("Hello", text);
//    }

    @Test
    public void testGetBucket() {
    }

    @Test
    public void testAllCallsFailInTransaction() {
        Context.put("tests",1);
        try {
            instance.get("testbucket", "fileSystemFile");
            Assert.fail("Was expecting an exception. Should not get here");
        }
        catch(IllegalStateException e){

        }finally{
            Context.clear();
        }
    }

    @Test
    public void testNoReadingWhileInTransaction() throws IOException{
        ReadableByteChannel reader = instance.reader("testbucket","mytest.txt");//what file should we read?
        Context.put("tests",1);
        try {
            int read = reader.read(ByteBuffer.allocateDirect(2048));//how to read using readableByteChannel.
            Assert.fail("Was expecting an exception. Should not get here");
        }
        catch(IllegalStateException e){

        } finally {
            Context.clear();
        }
    }

}
