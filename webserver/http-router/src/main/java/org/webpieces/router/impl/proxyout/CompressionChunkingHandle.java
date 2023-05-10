package org.webpieces.router.impl.proxyout;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.OverwritePlatformResponse;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.router.api.RouterResponseHandler;
import org.webpieces.router.impl.compression.Compression;
import org.webpieces.router.impl.compression.CompressionLookup;
import org.webpieces.router.impl.compression.MimeTypes;
import org.webpieces.router.impl.routeinvoker.WebSettings;
import org.webpieces.util.SneakyThrow;

import com.webpieces.http2.api.dto.highlevel.Http2Headers;
import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.dto.lowlevel.DataFrame;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2MsgType;
import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2.api.streaming.PushStreamHandle;
import com.webpieces.http2.api.streaming.StreamWriter;

/**
 * NOTE: All of these pieces SHOULD move into FrontendManager so that anyone uses frontend gets compression and keep alive stuff for
 * free!!!  We are consolidating here for a move.  Keep alive is still not handled here yet
 * 
 * @author dean
 *
 */
public class CompressionChunkingHandle implements RouterResponseHandler {
    private RouterResponseHandler handler;
    private MimeTypes mimeTypes;
    private CompressionLookup compressionLookup;
    private Http2Response lastResponseSent;
	private boolean compressionOff;
	private Http2Request originalRequest;
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

	public void init(RouterResponseHandler handler, Http2Request req) {
		this.handler = handler;
		this.originalRequest = req;
	}
	
	public void setRouterRequest(RouterRequest routerRequest) {
		this.routerRequest = routerRequest;
	}
	
    @Override
    public XFuture<StreamWriter> process(Http2Response response) {
        if(lastResponseSent != null)
            throw new IllegalStateException("You already sent a response.  "
                    + "do not call Actions.redirect or Actions.render more than once.  previous response="
                    + lastResponseSent +" 2nd response="+response);
        lastResponseSent = response;

        Compression compression = checkForCompression(response);
		ChunkedStream chunkedStream = new ChunkedStream(webSettings.getMaxBodySizeToSend());
			
		Http2Response finalResp = response;
		if(Current.isContextSet()) {
			//in some exceptional cases like incoming cookies failing to parse, there will be no context
			List<OverwritePlatformResponse> callbacks = Current.getContext().getCallbacks();
			for(OverwritePlatformResponse callback : callbacks) {
				finalResp = (Http2Response)callback.modifyOrReplace(finalResp);
			}
		}
		
		boolean closeAfterResponding = false;
		if(closeAfterResponding(originalRequest))
			closeAfterResponding = true;
		
		boolean shouldClose = closeAfterResponding;
		
        return handler.process(response)
        		.thenApply(w -> possiblyClose(shouldClose, response, w))
                .thenApply(w -> new ProxyStreamWriter(shouldClose, compression, chunkedStream, w));
    }

    
    
	private StreamWriter possiblyClose(boolean closeAfterResponding, Http2Response response, StreamWriter w) {
		if(closeAfterResponding && response.isEndOfStream())
			this.closeIfNeeded();
		
		return w;
	}

	public boolean closeAfterResponding(Http2Headers request) {
		String connHeader = request.getSingleHeaderValue(Http2HeaderName.CONNECTION);
		boolean close = false;
		if(!"keep-alive".equals(connHeader)) {
			close = true;
		} else
			close = false;

		return close;
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

    private class ProxyStreamWriter implements StreamWriter {

		private ChunkedStream chunkedStream;
		private StreamWriter w;
		private OutputStream chainStream;
		private boolean shouldClose;

		public ProxyStreamWriter(boolean shouldClose, Compression compression, ChunkedStream chunkedStream, StreamWriter w) {
			this.shouldClose = shouldClose;
			this.chunkedStream = chunkedStream;
			this.w = w;
			chainStream = compression.createCompressionStream(chunkedStream);
		}

		@Override
        public XFuture<Void> processPiece(StreamMsg data) {
			if(data.getMessageType() == Http2MsgType.DATA) {
				return processData((DataFrame)data);
			}
			
			return w.processPiece(data);
		}
		
		public XFuture<Void> processData(DataFrame frame) {
			boolean eos = frame.isEndOfStream();
			
			DataWrapper data = frame.getData();
			byte[] bytes = data.readBytesAt(0, data.getReadableSize());
			try {
				chainStream.write(bytes);
			} catch (IOException e) {
				throw SneakyThrow.sneak(e);
			}

			if(eos) {
				//closing flushes the stream to frames
				try {
					chainStream.close();
				} catch (IOException e) {
					throw SneakyThrow.sneak(e);
				}
			}
			
			List<DataFrame> frames = chunkedStream.getFrames();
			
			XFuture<Void> future = XFuture.completedFuture(null);

			for(int i = 0; i < frames.size(); i++) {
				DataFrame f = frames.get(i);
				XFuture<Void> fut;
				if(eos && i == frames.size()-1) {
					//IF client sent LAST frame, we must mark last frame we are sending as last frame too
					f.setEndOfStream(true);
					fut = w.processPiece(f).thenApply(voidd -> maybeClose());
				} else {
					fut = w.processPiece(f);	
				}
				
				 
				//compose AFTER processPiece so we call processPiece FAST N times, then
				//add to future's last result
				future = future.thenCompose(s -> fut);
			}
			
			return future;
        }

		private Void maybeClose() {
			if(shouldClose)
				closeIfNeeded();
			
			return null;
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
    public XFuture<Void> cancel(CancelReason payload) {
        return handler.cancel(payload);
    }

	public void turnCompressionOff() {
		this.compressionOff = true;
	}

	@Override
	public void closeSocket(String reason) {
		handler.closeSocket(reason);
	}

}
