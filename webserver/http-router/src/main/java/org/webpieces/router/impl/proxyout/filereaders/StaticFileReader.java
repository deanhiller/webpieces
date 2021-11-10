package org.webpieces.router.impl.proxyout.filereaders;

import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.impl.dto.RenderStaticResponse;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routeinvoker.NullWriter;

import com.webpieces.http2.api.streaming.StreamWriter;

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


	public XFuture<StreamWriter> sendRenderStatic(RequestInfo info, RenderStaticResponse renderStatic, ProxyStreamHandle handle) {
		try {
			if(renderStatic.isOnClassPath())
				return classPathFileReader.runFileRead(info, renderStatic, handle).thenApply(s -> new NullWriter());
			return fileSystemReader.runFileRead(info, renderStatic, handle).thenApply(s -> new NullWriter());
		} catch(Throwable e) {
			XFuture<StreamWriter> failed = new XFuture<StreamWriter>();
			failed.completeExceptionally(e);
			return failed;
		}
	}

}
