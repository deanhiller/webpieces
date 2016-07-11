package org.webpieces.util.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

public class VirtualFileClasspath implements VirtualFile {

	
	private URL resource;
	private String path;

	public VirtualFileClasspath(String path, ClassLoader classLoader) {
		resource = classLoader.getResource(path);
		this.path = path;
	}

	@Override
	public boolean isDirectory() {
		return false;
	}

	@Override
	public String getName() {
		throw new UnsupportedOperationException("not yet");
	}

	@Override
	public List<VirtualFile> list() {
		throw new UnsupportedOperationException("not yet");
	}

	@Override
	public String contentAsString(Charset charset) {
		//all this to avoid IOUtils.getString(inputStream, charset)
		//is it worth it ?  
		try {
			StringBuilder builder = new StringBuilder();
			try (InputStream str = openInputStream();
				InputStreamReader r = new InputStreamReader(str, charset);
				BufferedReader br = new BufferedReader(r)) {

		        for(String line=br.readLine(); line!=null; line=br.readLine()) {
	                builder.append(line);
	                builder.append('\n');
	            }
				
				return builder.toString();
			}
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long lastModified() {
		throw new UnsupportedOperationException("not yet");
	}

	@Override
	public VirtualFile child(String fileName) {
		throw new UnsupportedOperationException("not yet");
	}

	@Override
	public boolean exists() {
		return resource != null;
	}

	@Override
	public InputStream openInputStream() {
		try {
			return resource.openStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getAbsolutePath() {
		return resource.toExternalForm();
	}

	@Override
	public URL toURL() {
		return resource;
	}

	@Override
	public long length() {
		throw new UnsupportedOperationException("not yet");
	}

	@Override
	public boolean mkdirs() {
		throw new UnsupportedOperationException("not yet");
	}

	@Override
	public OutputStream openOutputStream() {
		throw new UnsupportedOperationException("not yet");
	}

	@Override
	public boolean delete() {
		throw new UnsupportedOperationException("not yet");
	}

	@Override
	public String getCanonicalPath() {
		return getAbsolutePath();
	}

	@Override
	public String toString() {
		String resourceStr = null;
		if(resource != null) 
			resourceStr = resource.toExternalForm();
			
		return "[VirtualFileClasspath: path="+path+" resource="+resourceStr+"]";
	}

}
