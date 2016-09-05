package org.webpieces.router.impl.compression;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

public class FileUtil {
	private static final DataWrapperGenerator wrapperFactory = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	public byte[] readFileContents(FileInputStream in, String urlPath, File src) throws IOException {
		DataWrapper fileContents = wrapperFactory.emptyWrapper();
		byte[] data = new byte[4000];
		int read;
		while((read = in.read(data)) > 0) {
			DataWrapper piece = wrapperFactory.wrapByteArray(data, 0, read);
			fileContents = wrapperFactory.chainDataWrappers(fileContents, piece);
		}
		
		byte[] allData = fileContents.createByteArray();
		return allData;
	}

	public void writeFile(OutputStream compressionOut, byte[] allData, String urlPath, File src) throws IOException {
		compressionOut.write(allData, 0, allData.length);
	}
}
