package org.webpieces.router.impl.routeinvoker;

import org.webpieces.util.futures.XFuture;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.impl.body.BodyParser;
import org.webpieces.router.impl.body.BodyParsers;
import org.webpieces.router.impl.dto.RouteType;

import com.webpieces.http2.api.dto.highlevel.Http2Headers;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.dto.lowlevel.DataFrame;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;
import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2.api.streaming.StreamWriter;

public class RequestStreamWriter2 implements StreamWriter {

    private static final Logger log = LoggerFactory.getLogger(RequestStreamWriter2.class);

    //TODO(dhiller): Remove static and inject so bugs can be fixed in these...
    private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    private Http2Headers trailingHeaders;
    private BodyParsers requestBodyParsers;
    private DataWrapper data = dataGen.emptyWrapper();

    private boolean cancelled;
    private XFuture<Void> responseFuture = new XFuture<>();

	private MethodMeta meta;
	private Function<MethodMeta, XFuture<Void>> invoker;

    public RequestStreamWriter2(
    		BodyParsers bodyParsers, MethodMeta meta, Function<MethodMeta, XFuture<Void>> invoker) {
		this.requestBodyParsers = bodyParsers;
		this.meta = meta;
		this.invoker = invoker;
    }

    @Override
    public XFuture<Void> processPiece(StreamMsg frame) {

        if(cancelled) {
            return XFuture.completedFuture(null);
        } else if(frame instanceof CancelReason) {
            cancelled = true;
            responseFuture.cancel(true);
        } else if(frame instanceof DataFrame) {
            DataFrame dataFrame = (DataFrame) frame;
            data = dataGen.chainDataWrappers(data, dataFrame.getData());
        } else if(frame instanceof Http2Headers) {
            if(!frame.isEndOfStream())
                throw new IllegalArgumentException("Trailing headers from client must have end of stream set");
            trailingHeaders = (Http2Headers) frame;
        } else {
            throw new IllegalStateException("frame not expected=" + frame);
        }

        if(frame.isEndOfStream()) {
            return handleCompleteRequestImpl();
        }

        //return immediately resolved as we need more data to form request
        return XFuture.completedFuture(null);
    }

    private XFuture<Void> handleCompleteRequestImpl() {

        RouterRequest request = meta.getCtx().getRequest();

        request.body = data;

        if(meta.getRouteType() != RouteType.CONTENT)
        	parseBody(request.originalRequest, request);
        
        request.trailingHeaders = trailingHeaders;

        responseFuture = invoker.apply(meta);

        return responseFuture;
    }

    private void parseBody(Http2Headers req, RouterRequest routerRequest) {
        String lengthHeader = req.getSingleHeaderValue(Http2HeaderName.CONTENT_LENGTH);

        if(lengthHeader != null) {
            //Integer.parseInt(lengthHeader.getValue()); should not fail as it would have failed earlier in the parser when
            //reading in the body
            routerRequest.contentLengthHeaderValue = Integer.parseInt(lengthHeader);
        }

        parseBodyFromContentType(routerRequest);
    }

    /**
     * This has to be above LoginFilter so LoginFilter can flash the multiPartParams so edits exist through
     * a login!!  This moves body to the muliPartParams Map which LoginFilter uses
     */
    private void parseBodyFromContentType(RouterRequest req) {
        if(req.contentLengthHeaderValue == null)
            return;
        else if(req.contentLengthHeaderValue == 0)
            return;

        if(req.contentTypeHeaderValue == null) {
            log.info("Incoming content length was specified, but no contentType was(We will not parse the body).  req="+req+" httpReq="+req.originalRequest);
            return;
        }

        BodyParser parser = requestBodyParsers.lookup(req.contentTypeHeaderValue.getContentType());
        if(parser == null) {
            log.error("Incoming content length was specified but content type was not 'application/x-www-form-urlencoded'(We will not parse body).  req="+req+" httpReq="+req.originalRequest);
            return;
        }

        DataWrapper body = req.body;
        parser.parse(body, req);
    }

}
