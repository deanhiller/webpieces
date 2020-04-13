package org.webpieces.httpclient.api.mocks;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;

import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.lib.StreamMsg;

public class MockStreamWriter extends MockSuperclass implements StreamWriter {

	enum Method implements MethodEnum {
		INCOMING_DATA
	}
	
	public MockStreamWriter() {
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<Void> processPiece(StreamMsg data) {
		return (CompletableFuture<Void>) super.calledMethod(Method.INCOMING_DATA, data);
	}

	public List<StreamMsg> getFrames() {
		Stream<ParametersPassedIn> calledMethodList = super.getCalledMethods(Method.INCOMING_DATA);
		Stream<StreamMsg> retVal = calledMethodList.map(p -> (StreamMsg)p.getArgs()[0]);
		return retVal.collect(Collectors.toList());
	}
	
	public StreamMsg getSingleFrame() {
		List<StreamMsg> frames = getFrames();
		if(frames.size() != 1)
			throw new IllegalStateException("There is not exactly one return value like expected.  num times method called="+frames.size());
		return frames.get(0);
	}

	public void addProcessResponse(CompletableFuture<Void> future2) {
		super.addValueToReturn(Method.INCOMING_DATA, future2);
	}

}
