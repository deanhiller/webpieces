package org.webpieces.googlecloud.storage.mock;

import org.webpieces.googlecloud.storage.impl.LocalStorage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MockGCPStorage extends LocalStorage implements Serializable {

    private transient Path tempDir;

    public MockGCPStorage() {

    }

    public ReadableByteChannel getReadChannel(String bucket, String filename) {

        if(filename == null) {
            throw new IllegalArgumentException("'filename' cannot be null!");
        }

        Path path = getLocalStoragePath(bucket, filename);

        if(Files.exists(path)) {
            try {
                return Files.newByteChannel(path);
            }
            catch(IOException ex) {
                throw SneakyThrow.sneak(ex);
            }
        }

        InputStream in = getClass().getClassLoader().getResourceAsStream(filename);

        if(in == null) {
            throw new IllegalArgumentException("'" + filename + "' not found in src/test/resources");
        }

        return Channels.newChannel(in);

    }

    @Override
    protected Path getLocalStoragePath(String bucket, String filename) {
        File f = new File("build/csv-output/"+bucket+"/"+filename);
        return f.toPath();
    }

    private Path getTempDir() {
        try {
            return Files.createTempDirectory("mockgcpstorage-");
        }
        catch(IOException ex) {
            throw SneakyThrow.sneak(ex);
        }
    }

    @Override
    public URI getFileInZip(String bucket, String filename) {
        return URI.create("jar:file:" + Paths.get("").toAbsolutePath().toString() + "/build/resources/test/" + bucket + "/" + filename);
    }

    public boolean doesMockFileExist(String bucket, String filename){
        return Files.exists(getLocalStoragePath(bucket, filename));
    }

}
