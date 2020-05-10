package org.webpieces.router.api;

import org.webpieces.util.file.FileFactory;

import java.io.File;

public class PrecompressedCache {
    public static File getCacheLocation() {
        return FileFactory.newCacheLocation("general/precompressedFiles");
    }
}
