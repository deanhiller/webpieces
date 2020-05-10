package org.webpieces.router.api;

import java.io.File;

import org.webpieces.util.file.FileFactory;

public class PrecompressedCache2 {
    public static File getCacheLocation(String projectName) {
        return FileFactory.newCacheLocation(projectName+"/precompressedFiles");
    }
}
