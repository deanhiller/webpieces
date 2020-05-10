package org.webpieces.router.impl.proxyout;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RouterStreamHandle;
import org.webpieces.router.impl.dto.RedirectResponse;
import org.webpieces.router.impl.proxyout.ResponseCreator.ResponseEncodingTuple;
import org.webpieces.router.impl.routeinvoker.ContextWrap;
import org.webpieces.router.impl.routers.ExceptionWrap;
import org.webpieces.util.futures.FutureHelper;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.PushStreamHandle;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.StatusCode;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

public class ProxyStreamHandle implements RouterStreamHandle {
	private static final Logger log = LoggerFactory.getLogger(ProxyStreamHandle.class);

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	private CompressionChunkingHandle handle;
	private ResponseCreator responseCreator;

	private ChannelCloser channelCloser;

	private FutureHelper futureUtil;

	@Inject
    public ProxyStreamHandle(
    	CompressionChunkingHandle handle,
    	ResponseCreator responseCreator,
    	ChannelCloser channelCloser,
    	FutureHelper futureUtil
    ) {
		this.handle = handle;
		this.responseCreator = responseCreator;
		this.channelCloser = channelCloser;
		this.futureUtil = futureUtil;
    }

	public void setRouterRequest(RouterRequest routerRequest) {
		handle.setRouterRequest(routerRequest);
	}

	public void init(RouterStreamHandle originalHandle) {
		handle.init(originalHandle);
	}
	
	public void turnCompressionOff() {
		handle.turnCompressionOff();
	}
	
    public CompletableFuture<StreamWriter> sendRedirectAndClearCookie(RouterRequest req, String badCookieName) {
        RedirectResponse httpResponse = new RedirectResponse(false, req.isHttps, req.domain, req.port, req.relativePath);
        Http2Response response = responseCreator.createRedirect(req.orginalRequest, httpResponse);

        responseCreator.addDeleteCookie(response, badCookieName);

        log.info("sending REDIRECT(due to bad cookie) response responseSender="+ this);
        CompletableFuture<StreamWriter> future = process(response);

        closeIfNeeded(req.orginalRequest);

        return future.thenApply(s -> null);
    }
    
	public Void closeIfNeeded(Http2Headers request) {
		String connHeader = request.getSingleHeaderValue(Http2HeaderName.CONNECTION);
		boolean close = false;
		if(!"keep-alive".equals(connHeader)) {
			close = true;
		} else
			close = false;
		
		if(close)
			closeIfNeeded();
		
		return null;
	}
	
    @Override
    public CompletableFuture<StreamWriter> process(Http2Response response) {
    	return handle.process(response);
    }

    @Override
    public Object getSocket() {
        return handle.getSocket();
    }

    @Override
    public Map<String, Object> getSession() {
        return handle.getSession();
    }

    @Override
    public boolean requestCameFromHttpsSocket() {
        return handle.requestCameFromHttpsSocket();
    }

    @Override
    public boolean requestCameFromBackendSocket() {
        return handle.requestCameFromBackendSocket();
    }

    @Deprecated
    @Override
    public Void closeIfNeeded() {
        return handle.closeIfNeeded();
    }

    @Override
    public PushStreamHandle openPushStream() {
        return handle.openPushStream();
    }

    @Override
    public CompletableFuture<Void> cancel(CancelReason payload) {
        return handle.cancel(payload);
    }

	public boolean hasSentResponseAlready() {
		return handle.hasSentResponseAlready();
	}

	public Throwable finalFailure(Throwable e, RequestContext requestCtx, ResponseStreamer proxy) {
		if(ExceptionWrap.isChannelClosed(e))
			return e;

		log.error("This is a final(secondary failure) trying to render the Internal Server Error Route", e);

		CompletableFuture<Void> future = futureUtil.syncToAsyncException(
				() -> failureRenderingInternalServerErrorPage(requestCtx, e, proxy)
		);
		
		future.exceptionally((t) -> {
			log.error("Webpieces failed at rendering it's internal error page since webapps internal erorr app page failed", t);
			return null;
		});
		return e;
	}

    public CompletableFuture<Void> failureRenderingInternalServerErrorPage(RequestContext ctx, Throwable e, ResponseStreamer proxyResponse) {
        return ContextWrap.wrap(ctx, () -> proxyResponse.failureRenderingInternalServerErrorPage(e));
    }

	public CompletableFuture<Void> createResponseAndSend(StatusCode statusCode, String content, String extension, String defaultMime) {
		if(content == null)
			throw new IllegalArgumentException("content cannot be null");
		
		Http2Request request = handle.getRouterRequest().orginalRequest;
		ResponseEncodingTuple tuple = responseCreator.createResponse(request, statusCode, extension, defaultMime, true);
		
		if(log.isDebugEnabled())
			log.debug("content about to be sent back="+content);
		
		Charset encoding = tuple.mimeType.htmlResponsePayloadEncoding;
		byte[] bytes = content.getBytes(encoding);
		
		return maybeCompressAndSend(extension, tuple, bytes);
	}
	
	public CompletableFuture<Void> maybeCompressAndSend(String extension, ResponseEncodingTuple tuple, byte[] bytes) {
		Http2Response resp = tuple.response;
		
		if(bytes.length == 0) {
			resp.setEndOfStream(true);
			return process(resp).thenApply(w -> null);
		}
		
		return sendChunkedResponse(resp, bytes);
	}

	private CompletableFuture<Void> sendChunkedResponse(Http2Response resp, byte[] bytes) {

		RouterRequest routerRequest = handle.getRouterRequest();
		log.info("sending RENDERHTML response. size="+bytes.length+" code="+resp+" for domain="+routerRequest.domain+" path"+routerRequest.relativePath+" responseSender="+ this);

		// Send the headers and get the responseid.
		return process(resp)
				.thenCompose(w -> createFrame(bytes, w));
	}

	private CompletionStage<Void> createFrame(byte[] bytes, StreamWriter writer) {
		DataFrame frame = new DataFrame();
		frame.setEndOfStream(true);
		frame.setData(dataGen.wrapByteArray(bytes));
		
		return writer.processPiece(frame);
	}

}
