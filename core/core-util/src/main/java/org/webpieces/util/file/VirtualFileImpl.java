package org.webpieces.util.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.webpieces.util.exceptions.SneakyThrow;

public class VirtualFileImpl implements VirtualFile {

	private File file;

	public VirtualFileImpl(File file) {
		//per http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4117557 
		//or https://stackoverflow.com/questions/2275362/java-file-exists-inconsistencies-when-setting-user-dir
		if(!file.isAbsolute())
			throw new IllegalArgumentException("All paths must be absolute since user.dir property cannot be modified");
		this.file = file;
	}
	
	@Override
	public boolean isDirectory() {
		return file.isDirectory();
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public List<VirtualFile> list() {
		File[] files = file.listFiles();
		List<VirtualFile> theFiles = new ArrayList<>();
		for(File f : files) {
			theFiles.add(new VirtualFileImpl(f));
		}
		return theFiles;
	}

	@Override
	public String contentAsString(Charset charset) {
		try {
			return new String(Files.readAllBytes(file.toPath()), charset);
		} catch (IOException e) {
			throw SneakyThrow.sneak(e);
		}
	}

	@Override
	public long lastModified() {
		return file.lastModified();
	}

	@Override
	public VirtualFile child(String fileName) {
		File child = FileFactory.newFile(file, fileName);
		return new VirtualFileImpl(child);
	}

	@Override
	public boolean exists() {
		return file.exists();
	}

	@Override
	public InputStream openInputStream() {
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw SneakyThrow.sneak(e);
		}
	}

	@Override
	public String toString() {
		return "[file=" + getCanonicalPath() + "]";
	}

	@Override
	public String getAbsolutePath() {
		return file.getAbsolutePath();
	}

	@Override
	public String getCanonicalPath() {
		try {
			return file.getCanonicalPath();
		} catch (IOException e) {
			throw SneakyThrow.sneak(e);
		}
	}
	
	@Override
	public URL toURL() {
		try {
			return file.toURI().toURL();
		} catch (MalformedURLException e) {
			throw SneakyThrow.sneak(e);
		}
	}

	@Override
	public long length() {
		return file.length();
	}

	@Override
	public boolean mkdirs() {
		return file.mkdirs();
	}

	@Override
	public OutputStream openOutputStream() {
		try {
			return new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			throw SneakyThrow.sneak(e);
		}
	}

	@Override
	public boolean delete() {
		return file.delete();
	}

	@Override
	public boolean isFile() {
		return file.isFile();
	}

}
