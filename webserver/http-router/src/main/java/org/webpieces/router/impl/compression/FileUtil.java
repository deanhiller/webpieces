package org.webpieces.router.impl.compression;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.webpieces.util.file.VirtualFile;

public class FileUtil {

	/**
	 * Ideally, we should NOT have urlPath here but for testing, it allows us to verify some very useful information so we 
	 * don't break a very important piece of the system that we would never see broken in dev server mode. 
	 */
	public byte[] readFileContents(String urlPath, VirtualFile srcFile) throws IOException {
		
		long len = srcFile.length();
		if(len > Integer.MAX_VALUE)
			throw new UnsupportedOperationException("File to large to process");
		
		try (InputStream in = srcFile.openInputStream()) {
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

	public void writeFile(OutputStream compressionOut, byte[] allData, String urlPath, VirtualFile src) throws IOException {
		compressionOut.write(allData, 0, allData.length);
	}
}
