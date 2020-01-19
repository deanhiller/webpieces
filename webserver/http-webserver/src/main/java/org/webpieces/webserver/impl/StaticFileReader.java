package org.webpieces.webserver.impl;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.impl.dto.RenderStaticResponse;
import org.webpieces.webserver.impl.filereaders.XFileReaderClasspath;
import org.webpieces.webserver.impl.filereaders.XFileReaderFileSystem;

@Singleton
public class StaticFileReader {

	private final XFileReaderFileSystem fileSystemReader;
	private final XFileReaderClasspath classPathFileReader;
	
	@Inject
	public StaticFileReader(XFileReaderFileSystem fileSystemReader, XFileReaderClasspath classPathFileReader) {
		super();
		this.fileSystemReader = fileSystemReader;
		this.classPathFileReader = classPathFileReader;
	}


	public CompletableFuture<Void> sendRenderStatic(RequestInfo info, RenderStaticResponse renderStatic) {
		try {
			if(renderStatic.isOnClassPath())
				return classPathFileReader.runFileRead(info, renderStatic);
			return fileSystemReader.runFileRead(info, renderStatic);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

}
