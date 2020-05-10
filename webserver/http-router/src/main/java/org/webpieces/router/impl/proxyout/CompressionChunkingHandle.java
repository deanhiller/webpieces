package org.webpieces.router.impl.proxyout;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.router.api.RouterStreamHandle;
import org.webpieces.router.impl.compression.Compression;
import org.webpieces.router.impl.compression.CompressionLookup;
import org.webpieces.router.impl.compression.MimeTypes;
import org.webpieces.router.impl.routeinvoker.WebSettings;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.PushStreamHandle;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;
import com.webpieces.http2parser.api.dto.lib.Http2MsgType;
import com.webpieces.http2parser.api.dto.lib.StreamMsg;

public class CompressionChunkingHandle implements RouterStreamHandle {
    private RouterStreamHandle handler;
    private MimeTypes mimeTypes;
    private CompressionLookup compressionLookup;
    private Http2Response lastResponseSent;
	private boolean compressionOff;
	private RouterRequest routerRequest;
	private WebSettings webSettings;
	
	@Inject
    public CompressionChunkingHandle(
        MimeTypes mimeTypes,
        CompressionLookup compressionLookup,
        WebSettings webSettings
    ) {
        this.mimeTypes = mimeTypes;
        this.compressionLookup = compressionLookup;
		this.webSettings = webSettings;
    }

	public void setRouterRequest(RouterRequest routerRequest) {
		this.routerRequest = routerRequest;
	}

	public void init(RouterStreamHandle handler) {
		this.handler = handler;
	}
	
    @Override
    public CompletableFuture<StreamWriter> process(Http2Response response) {
        if(lastResponseSent != null)
            throw new IllegalStateException("You already sent a response.  "
                    + "do not call Actions.redirect or Actions.render more than once.  previous response="
                    + lastResponseSent +" 2nd response="+response);
        lastResponseSent = response;

        Compression compression = checkForCompression(response);
		ChunkedStream chunkedStream = new ChunkedStream(webSettings.getMaxBodySizeToSend());
				
        return handler.process(response)
                .thenApply(w -> new ProxyStreamWriter(compression, chunkedStream, w));
    }

    private Compression checkForCompression(Http2Response response) {
    	if(routerRequest == null) {
    		//The exception happened BEFORE Http2Request Accept-Encoding Header encodings were parsed which we HAVE to know
    		//as that is what the client accepts for compression
    		return new NoCompression();
    	}
    	
    	if(compressionOff) {
    		//Some contentRouters precompress their content so they are responsible for checking the accept header and picking
    		//a file that is already compressed.  In this case, don't compress on top of their cached compression
    		return new NoCompression();
    	}
		
      	Http2Header header = response.getHeaderLookupStruct().getHeader(Http2HeaderName.CONTENT_TYPE);
      	String contentType = response.getSingleHeaderValue(Http2HeaderName.CONTENT_TYPE);
      	if(contentType == null) //could be a redirect or something with 0 bytes anyways or we don't know what it is so don't compress
      		return new NoCompression();
      	
        MimeTypes.MimeTypeResult mimeType = mimeTypes.createMimeType(header.getValue());

        Compression compression = compressionLookup.createCompressionStream(routerRequest.encodings, mimeType);

        if (compression == null) {
        	return new NoCompression();
        } else {
            response.addHeader(new Http2Header(Http2HeaderName.CONTENT_ENCODING, compression.getCompressionType()));
            return compression;
        }
	}
	
	public boolean hasSentResponseAlready() {
        return lastResponseSent != null;
    }

    private static class ProxyStreamWriter implements StreamWriter {

		private ChunkedStream chunkedStream;
		private StreamWriter w;
		private OutputStream chainStream;

		public ProxyStreamWriter(Compression compression, ChunkedStream chunkedStream, StreamWriter w) {
			this.chunkedStream = chunkedStream;
			this.w = w;
			chainStream = compression.createCompressionStream(chunkedStream);
		}

		@Override
        public CompletableFuture<Void> processPiece(StreamMsg data) {
			if(data.getMessageType() == Http2MsgType.DATA) {
				return processData((DataFrame)data);
			}
			
			return w.processPiece(data);
		}
		
		public CompletableFuture<Void> processData(DataFrame frame) {
			boolean eos = frame.isEndOfStream();
			
			DataWrapper data = frame.getData();
			byte[] bytes = data.readBytesAt(0, data.getReadableSize());
			try {
				chainStream.write(bytes);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			if(eos) {
				//closing flushes the stream to frames
				try {
					chainStream.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			
			List<DataFrame> frames = chunkedStream.getFrames();
			
			CompletableFuture<Void> future = CompletableFuture.completedFuture(null);

			for(int i = 0; i < frames.size(); i++) {
				DataFrame f = frames.get(i);
				if(eos && i == frames.size()-1) {
					//IF client sent LAST frame, we must mark last frame we are sending as last frame too
					f.setEndOfStream(true);
				}
				
				CompletableFuture<Void> fut = w.processPiece(f);
				//compose AFTER processPiece so we call processPiece FAST N times, then
				//add to future's last result
				future = future.thenCompose(s -> fut);
			}
			
			return future;
        }
    }

    @Override
    public Object getSocket() {
        return handler.getSocket();
    }

    @Override
    public Map<String, Object> getSession() {
        return handler.getSession();
    }

    @Override
    public boolean requestCameFromHttpsSocket() {
        return handler.requestCameFromHttpsSocket();
    }

    @Override
    public boolean requestCameFromBackendSocket() {
        return handler.requestCameFromBackendSocket();
    }

    @Deprecated
    @Override
    public Void closeIfNeeded() {
        return handler.closeIfNeeded();
    }

    @Override
    public PushStreamHandle openPushStream() {
        return handler.openPushStream();
    }

    @Override
    public CompletableFuture<Void> cancel(CancelReason payload) {
        return handler.cancel(payload);
    }

	public void turnCompressionOff() {
		this.compressionOff = true;
	}
}
