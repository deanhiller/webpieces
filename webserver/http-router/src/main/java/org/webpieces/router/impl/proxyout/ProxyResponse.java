package org.webpieces.router.impl.proxyout;

import java.io.StringWriter;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.MissingPropException;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.TemplateApi;
import org.webpieces.router.api.exceptions.ControllerPageArgsException;
import org.webpieces.router.api.exceptions.WebSocketClosedException;
import org.webpieces.router.impl.dto.RenderResponse;
import org.webpieces.router.impl.dto.View;
import org.webpieces.util.exceptions.NioClosedChannelException;
import org.webpieces.util.futures.FutureHelper;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2parser.api.dto.StatusCode;

//MUST NOT BE @Singleton!!! since this is created per request
public class ProxyResponse implements ResponseStreamer {

	private static final Logger log = LoggerFactory.getLogger(ProxyResponse.class);
	
	private final TemplateApi templatingService;
	
	private ProxyStreamHandle stream;
	private Http2Request request;

	private FutureHelper futureUtil;

	@Inject
	public ProxyResponse(
		TemplateApi templatingService, 
		FutureHelper futureUtil
	) {
		super();
		this.templatingService = templatingService;
		this.futureUtil = futureUtil;
	}

	public void init(RouterRequest req, ProxyStreamHandle responseSender) {
		this.request = req.originalRequest;
		this.stream = responseSender;
	}





}

