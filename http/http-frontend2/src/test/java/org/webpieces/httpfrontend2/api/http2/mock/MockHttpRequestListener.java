package org.webpieces.httpfrontend2.api.http2.mock;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.webpieces.frontend2.api.FrontendStream;
import org.webpieces.frontend2.api.HttpRequestListener;
import org.webpieces.frontend2.api.Protocol;
import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.RstStreamFrame;

public class MockHttpRequestListener extends MockSuperclass implements HttpRequestListener {

	private enum Method implements MethodEnum {
		INCOMING_FRAME
	}
	
	@Override
	public StreamWriter incomingRequest(FrontendStream stream, Http2Headers headers, Protocol type) {
		RequestData data = new RequestData(stream, headers, type);
		return (StreamWriter) super.calledMethod(Method.INCOMING_FRAME, data);
	}

	public RequestData getRequestDataAndClear() {
		List<RequestData> msgs = getRequestDatasAndClear();
		if(msgs.size() != 1)
			throw new IllegalStateException("not correct number of responses.  number="+msgs.size()+" but expected 1.  list="+msgs);
		return msgs.get(0);
	}
	
	public List<RequestData> getRequestDatasAndClear() {
		Stream<ParametersPassedIn> calledMethodList = super.getCalledMethods(Method.INCOMING_FRAME);
		Stream<RequestData> retVal = calledMethodList.map(p -> (RequestData)p.getArgs()[0]);

		//clear out read values
		this.calledMethods.remove(Method.INCOMING_FRAME);
		
		return retVal.collect(Collectors.toList());
	}

	@Override
	public void cancelRequest(FrontendStream stream, RstStreamFrame c) {
		// TODO Auto-generated method stub
		
	}

}