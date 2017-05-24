package org.webpieces.compiler.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

/**
 * Used to speed up compilation time
 */
public class BytecodeCache {

	private static final Logger log = LoggerFactory.getLogger(BytecodeCache.class);
	private CompileConfig config;
	
    public BytecodeCache(CompileConfig config) {
    	this.config = config;
	}

	/**
     * Delete the bytecode
     * @param name Cache name
     */
    public void deleteBytecode(String name) {
        VirtualFile f = cacheFile(name.replace("/", "_").replace("{", "_").replace("}", "_").replace(":", "_"));
        if (f.exists()) {
            f.delete();
        }
    }

    /**
     * Retrieve the bytecode if source has not changed
     * @param name The cache name
     * @param source The source code
     * @return The bytecode
     */
    public byte[] getBytecode(String name, String source) {
        try {
            VirtualFile f = cacheFile(name.replace("/", "_").replace("{", "_").replace("}", "_").replace(":", "_"));
            if (f.exists()) {
            	try (InputStream fis = f.openInputStream()) {
	                // Read hash
	                int offset = 0;
	                int read = -1;
	                StringBuilder hash = new StringBuilder();
	                // look for null byte, or end-of file
	                while ((read = fis.read()) > 0) {
	                    hash.append((char) read);
	                    offset++;
	                }
	                if (!hash(source).equals(hash.toString())) {
	
	                    log.trace(()->"Bytecode too old ("+hash+" != "+hash(source)+") for name="+name);
	                    return null;
	                }
	                byte[] byteCode = new byte[(int) f.length() - (offset + 1)];
	                fis.read(byteCode);
	                return byteCode;
            	}
            }

            log.trace(()->"Cache MISS for "+name);
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Cache the bytecode
     * @param byteCode The bytecode
     * @param name The cache name
     * @param source The corresponding source
     */
    public void cacheBytecode(byte[] byteCode, String name, String source) {
        try {
            VirtualFile f = cacheFile(name.replace("/", "_").replace("{", "_").replace("}", "_").replace(":", "_"));
            
            try (OutputStream fos = f.openOutputStream()) {
	            fos.write(hash(source).getBytes("utf-8"));
	            fos.write(0);
	            fos.write(byteCode);
            }

            log.trace(()->name + "cached");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Build a hash of the source code.
     * To efficiently track source code modifications.
     */
    String hash(String text) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(text.getBytes("utf-8"));
            byte[] digest = messageDigest.digest();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < digest.length; ++i) {
                int value = digest[i];
                if (value < 0) {
                    value += 256;
                }
                builder.append(Integer.toHexString(value));
            }
            return builder.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieve the real file that will be used as cache.
     */
    VirtualFile cacheFile(String id) {
    	VirtualFile dir = config.getByteCodeCacheDir();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir.child(id);
    }
}
