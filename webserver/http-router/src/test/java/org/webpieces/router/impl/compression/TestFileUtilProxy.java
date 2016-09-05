package org.webpieces.router.impl.compression;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class TestFileUtilProxy extends FileUtil {

	private List<FileInfo> readFiles = new ArrayList<>();
	private List<FileInfo> compressedFiles = new ArrayList<>();
	
	@Override
	public byte[] readFileContents(FileInputStream in, String urlPath, File src) throws IOException {
		readFiles.add(new FileInfo(urlPath, src));
		return super.readFileContents(in, urlPath, src);
	}

	@Override
	public void writeFile(OutputStream compressionOut, byte[] allData, String urlPath, File src) throws IOException {
		compressedFiles.add(new FileInfo(urlPath, src));
		super.writeFile(compressionOut, allData, urlPath, src);
	}

	public List<FileInfo> getReadFiles() {
		return readFiles;
	}

	public List<FileInfo> getCompressedFiles() {
		return compressedFiles;
	}
	
	public void clear() {
		readFiles.clear();
		compressedFiles.clear();
	}
}
