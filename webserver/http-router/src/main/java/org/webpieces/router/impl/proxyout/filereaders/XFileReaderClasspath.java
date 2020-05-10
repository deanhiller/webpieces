package org.webpieces.router.impl.proxyout.filereaders;

import java.io.InputStream;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.impl.dto.RenderStaticResponse;
import org.webpieces.router.impl.proxyout.ChannelCloser;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.proxyout.ResponseCreator;
import org.webpieces.util.file.VirtualFile;

import com.webpieces.hpack.api.dto.Http2Response;

public class XFileReaderClasspath extends XFileReader {

	private static final Logger log = LoggerFactory.getLogger(XFileReaderClasspath.class);

	@Inject
	public XFileReaderClasspath(
		ResponseCreator responseCreator,
		RouterConfig config,
		ChannelCloser channelCloser
	) {
		super(responseCreator, config, channelCloser);
	}
	
	@Override
	protected String getNameToUse(VirtualFile fullFilePath) {
		return fullFilePath.getAbsolutePath();
	}
	
	@Override
	protected ChunkReader createFileReader(Http2Response response, RenderStaticResponse renderStatic,
			String fileName, VirtualFile fullFilePath, RequestInfo info, String extension,
			ResponseCreator.ResponseEncodingTuple tuple, ProxyStreamHandle handle) {

		if(log.isDebugEnabled())
			log.debug("Opening class path file "+fullFilePath);
		InputStream inputStream = fullFilePath.openInputStream();

		return new ChunkClassPathReader(inputStream, fullFilePath);
	}

}
