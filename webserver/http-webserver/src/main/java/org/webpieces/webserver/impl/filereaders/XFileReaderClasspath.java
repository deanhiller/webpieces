package org.webpieces.webserver.impl.filereaders;

import java.io.InputStream;

import org.webpieces.router.impl.dto.RenderStaticResponse;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.webserver.impl.RequestInfo;
import org.webpieces.webserver.impl.ResponseCreator.ResponseEncodingTuple;

import com.webpieces.hpack.api.dto.Http2Response;

public class XFileReaderClasspath extends XFileReader {

	private static final Logger log = LoggerFactory.getLogger(XFileReaderClasspath.class);

	@Override
	protected String getNameToUse(VirtualFile fullFilePath) {
		return fullFilePath.getAbsolutePath();
	}
	
	@Override
	protected ChunkReader createFileReader(Http2Response response, RenderStaticResponse renderStatic,
			String fileName, VirtualFile fullFilePath, RequestInfo info, String extension,
			ResponseEncodingTuple tuple) {

		log.debug(() -> "Opening class path file "+fullFilePath);
		InputStream inputStream = fullFilePath.openInputStream();

		return new ChunkClassPathReader(inputStream, fullFilePath);
	}

}
