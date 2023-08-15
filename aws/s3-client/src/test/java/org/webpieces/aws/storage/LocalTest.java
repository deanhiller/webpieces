package org.webpieces.aws.storage;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.webpieces.aws.storage.api.AWSBlob;
import org.webpieces.aws.storage.api.AWSStorage;
import org.webpieces.util.context.Context;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalTest {

    private AWSStorage instance;

    @Before
    public void setup() {

        Module testModule = Modules.override(new TestProdModule()).with(new TestLocalModule());
        Injector injector = Guice.createInjector(testModule);
        instance = injector.getInstance(AWSStorage.class);

    }

    @After
    public void tearDown() {
        List<String> buckets = new ArrayList<>();
        buckets.add("testbucket");

        for(String bucket : buckets) {
            deleteFilesInBucket(bucket);
        }
    }

    private void deleteFilesInBucket(String bucket) {
        Stream<AWSBlob> list = instance.list(bucket);
        list.forEach(blob -> instance.delete(blob.getBucket(), blob.getKey()));
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

        writeFile("testbucket", "fileShit.txt");


        ReadableByteChannel channel = instance.reader("testbucket", "fileShit.txt");

        InputStream i = Channels.newInputStream(channel);

        String text = new BufferedReader(
                new InputStreamReader(i, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        Assert.assertEquals("testing a bitch", text);
    }

    private void writeFile(String bucket, String key) throws IOException {

        WritableByteChannel writer = instance.writer(bucket, key);
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
        writeFile("listbucket", "fileSystemFile1.txt");
        writeFile("listbucket", "fileSystemFile2.txt");

        Stream<AWSBlob> testbucket = instance.list("listbucket");

        List<String> keys = testbucket.map(blob -> blob.getKey()).collect(Collectors.toList());

        Collections.sort(keys);

        Assert.assertEquals("fileSystemFile1.txt", keys.get(0));
        Assert.assertEquals("fileSystemFile2.txt", keys.get(1));

    }

    @Test
    public void validateFileNotFoundReturnsNullBlob() {
        AWSBlob basket = instance.get("backet", "non-existent");
        Assert.assertNull(basket);
    }

    @Test
    public void testGetBlobClassPath() {
        AWSBlob testbucket = instance.get("testbucket", "mytest.txt");
        Assert.assertEquals("mytest.txt",testbucket.getKey());
    }
    
    @Test
    public void testGetBlobFileSystem() throws IOException {
        //create a file
        writeFile("testbucket", "fileSystemFile.txt");

        AWSBlob bucket = instance.get("testbucket", "fileSystemFile.txt");
        Assert.assertEquals("fileSystemFile.txt",bucket.getKey());

    }

    @Test
    public void addFileToBucketAndThenListFiles() throws IOException {
        instance.list("ListFilebucket");
        writeFile("ListFilebucket", "mytest1.txt");
        AWSBlob bucket = instance.get("ListFilebucket", "mytest1.txt");
        Assert.assertEquals("mytest1.txt",bucket.getKey());//passed.

        Stream<AWSBlob> listfilebucket = instance.list("ListFilebucket");
        List<String> list = listfilebucket.map(blob -> blob.getKey()).collect(Collectors.toList());

        Assert.assertEquals(1,list.size());

    }

    @Test
    public void addFileThenReadThenDeleteThenListFiles() throws IOException {
        writeFile("AddReadDeleteListbucket", "mytest1.txt");
        AWSBlob bucket = instance.get("AddReadDeleteListbucket", "mytest1.txt");
        Assert.assertEquals("mytest1.txt",bucket.getKey());//passed.

        ReadableByteChannel readFile = instance.reader("AddReadDeleteListbucket", "mytest1.txt");

        InputStream i = Channels.newInputStream(readFile);

        String text = new BufferedReader(
                new InputStreamReader(i, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        Assert.assertEquals("testing a bitch", text);// Passed.

        boolean success = instance.delete("AddReadDeleteListbucket", "mytest1.txt");
        Assert.assertEquals(true,success);//passed.

        Stream<AWSBlob> testbucket = instance.list("AddReadDeleteListbucket");
        List<String> list = testbucket.map(blob -> blob.getKey()).collect(Collectors.toList());

        //length of the blob should be original length.
        Assert.assertEquals(0,list.size());// testing on testbucket directory
        // with 2 files already existed.
    }

    @Test
    public void testCopyFromClassPath() throws IOException {

        String bucketName = "testbucket";
        String blobName = "mytest.txt";
        String copyBlobName = "mytest_copy.txt";

        boolean success = instance.copy(bucketName, blobName, "copybucket", copyBlobName);
        Assert.assertEquals(true, success);

        ReadableByteChannel readFile = instance.reader("copybucket", "mytest_copy.txt");

        InputStream i = Channels.newInputStream(readFile);

        String text = new BufferedReader(
                new InputStreamReader(i, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        Assert.assertEquals("Some Test", text);// Passed.
    }

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
