package org.webpieces.router.impl.compression;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FileUtil {

	/**
	 * Ideally, we should NOT have urlPath here but for testing, it allows us to verify some very useful information so we 
	 * don't break a very important piece of the system that we would never see broken in dev server mode. 
	 */
	public byte[] readFileContents(String urlPath, File src) throws IOException {
		long len = src.length();
		if(len > Integer.MAX_VALUE)
			throw new UnsupportedOperationException("File to large to process");
		
		try (FileInputStream in = new FileInputStream(src)) {
			int size = (int) len;
	
	        if (size == 0) {
	            return new byte[0];
	        }
	
	        final byte[] data = new byte[size];
	        int offset = 0;
	        int readed;
	
	        while (offset < size && (readed = in.read(data, offset, size - offset)) != -1) {
	            offset += readed;
	        }
	
	        if (offset != size) {
	            throw new IOException("Unexpected readed size. current: " + offset + ", excepted: " + size);
	        }
	
	        return data;
		}
	}

	public void writeFile(OutputStream compressionOut, byte[] allData, String urlPath, File src) throws IOException {
		compressionOut.write(allData, 0, allData.length);
	}
}
