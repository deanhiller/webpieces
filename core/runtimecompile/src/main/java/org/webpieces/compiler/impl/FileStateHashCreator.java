package org.webpieces.compiler.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.webpieces.util.file.VirtualFile;

public class FileStateHashCreator {

    private final Pattern classDefFinderPattern = Pattern.compile("\\s+class\\s([a-zA-Z0-9_]+)\\s+");

    //no need to release references as this whole class goes away with CompilingClassLoader
    private final Map<String, FileWithClassDefs> classDefsInFileCache = new HashMap<>();

    public synchronized int computePathHash(List<VirtualFile> paths) {
        StringBuffer buf = new StringBuffer();
        for (VirtualFile virtualFile : paths) {
            scan(buf, virtualFile);
        }
        
        // TODO: should use better hashing-algorithm.. MD5? SHA1?
        // I think hashCode() has too many collisions..
        return buf.toString().hashCode();
    }

    private void scan(StringBuffer buf, VirtualFile current) {
        if (!current.isDirectory()) {
            if (current.getName().endsWith(".java")) {
                buf.append( getClassDefsForFile(current));
            }
        } else if (!current.getName().startsWith(".")) {
            // TODO: we could later optimizie it further if we check if the entire folder is unchanged
            for (VirtualFile virtualFile : current.list()) {
                scan(buf, virtualFile);
            }
        }
    }

    private String getClassDefsForFile( VirtualFile current ) {

        String absolutePath = current.getAbsolutePath();
        // first we look in cache

        FileWithClassDefs fileWithClassDefs = classDefsInFileCache.get( absolutePath );
        if( fileWithClassDefs != null && fileWithClassDefs.fileNotChanges() ) {
            // found the file in cache and it has not changed on disk
            return fileWithClassDefs.getClassDefs();
        }

        // didn't find it or it has changed on disk
        // we must re-parse it

        StringBuilder buf = new StringBuilder();
        Matcher matcher = classDefFinderPattern.matcher(current.contentAsString());
        buf.append(current.getName());
        buf.append("(");
        while (matcher.find()) {
            buf.append(matcher.group(1));
            buf.append(",");
        }
        buf.append(")");
        String classDefs = buf.toString();

        // store it in cache
        classDefsInFileCache.put( absolutePath, new FileWithClassDefs(current, classDefs));
        return classDefs;
    }


    private static class FileWithClassDefs {
        private final VirtualFile file;
        private final long size;
        private final long lastModified;
        private final String classDefs;

        private FileWithClassDefs(VirtualFile file, String classDefs) {
            this.file = file;
            this.classDefs = classDefs;

            // store size and time for this file..
            size = file.length();
            lastModified = file.lastModified();
        }

        /**
         * @return true if file has changed on disk
         */
        public boolean fileNotChanges( ) {
            return size == file.length() && lastModified == file.lastModified();
        }

        public String getClassDefs() {
            return classDefs;
        }
    }
}
