package org.webpieces.router.impl;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.MDC;
import org.webpieces.router.api.RouterStreamHandle;
import org.webpieces.router.impl.compression.CompressionLookup;
import org.webpieces.router.impl.compression.MimeTypes;
import org.webpieces.util.futures.FutureHelper;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.PushStreamHandle;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;
import com.webpieces.http2parser.api.dto.lib.StreamMsg;

public class ProxyStreamHandle implements RouterStreamHandle {
    private String txId;
    private RouterStreamHandle handler;
    private FutureHelper futureUtil;
    private MimeTypes mimeTypes;
    private CompressionLookup compressionLookup;
    private Http2Response lastResponseSent;
	private boolean preCompressed;

    public ProxyStreamHandle(
        String txId,
        RouterStreamHandle handler,
        FutureHelper futureUtil,
        MimeTypes mimeTypes,
        CompressionLookup compressionLookup
    ) {
        this.txId = txId;
        this.handler = handler;
        this.futureUtil = futureUtil;
        this.mimeTypes = mimeTypes;
        this.compressionLookup = compressionLookup;
    }

    @Override
    public CompletableFuture<StreamWriter> process(Http2Response response) {
        if(lastResponseSent != null)
            throw new IllegalStateException("You already sent a response.  "
                    + "do not call Actions.redirect or Actions.render more than once.  previous response="
                    + lastResponseSent +" 2nd response="+response);
        lastResponseSent = response;

        boolean compressed = false;

//        if(!preCompressed) {
//        	Http2Header header = response.getHeaderLookupStruct().getHeader(Http2HeaderName.CONTENT_TYPE);
//        	if(header == null)
//        		throw new IllegalArgException("Response must contain a Content-Type header so we can determine compression");
//        	else if(header.getValue() == null)
//                throw new IllegalArgException("Response contains a Content-Type header with a null value which is not allowed");
//
//            MimeTypes.MimeTypeResult mimType = mimeTypes.createMimeType(header.getValue());
//
//            Compression compression = compressionLookup.createCompressionStream(routerRequest.encodings, mimeType);
//
//            Compression usingCompression;
//            if (compression == null) {
//                usingCompression = new NoCompression();
//            } else {
//                usingCompression = compression;
//                compressed = true;
//                response.addHeader(new Http2Header(Http2HeaderName.CONTENT_ENCODING, usingCompression.getCompressionType()));
//            }
//        }

        MDC.put("txId", txId);
        return handler.process(response)
                .thenApply(w -> new ProxyStreamWriter(txId, w));
    }

    public boolean hasSentResponseAlready() {
        return lastResponseSent != null;
    }

    private static class ProxyStreamWriter implements StreamWriter {

        private final String txId;
        private final StreamWriter w;

        public ProxyStreamWriter(String txId, StreamWriter w) {
            this.txId = txId;
            this.w = w;
        }

        @Override
        public CompletableFuture<Void> processPiece(StreamMsg data) {
            MDC.put("txId", txId);
            return w.processPiece(data);
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

	public void setPrecompressedStream(boolean preCompressed) {
		this.preCompressed = preCompressed;
	}
}
