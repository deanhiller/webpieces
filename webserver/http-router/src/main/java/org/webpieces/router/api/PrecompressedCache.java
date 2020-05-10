package org.webpieces.router.api;

import java.io.File;

import org.webpieces.util.file.FileFactory;

public class PrecompressedCache {
    public static File getCacheLocation() {
        return FileFactory.newCacheLocation("general/precompressedFiles");
    }
}
