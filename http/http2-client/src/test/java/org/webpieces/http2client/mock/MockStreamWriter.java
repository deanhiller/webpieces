package org.webpieces.http2client.mock;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;

import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class MockStreamWriter extends MockSuperclass implements StreamWriter {

	enum Method implements MethodEnum {
		INCOMING_DATA
	}
	
	public MockStreamWriter() {
		setDefaultReturnValue(Method.INCOMING_DATA, CompletableFuture.completedFuture(this));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<StreamWriter> processPiece(PartialStream data) {
		return (CompletableFuture<StreamWriter>) super.calledMethod(Method.INCOMING_DATA, data);
	}

	public List<PartialStream> getFrames() {
		Stream<ParametersPassedIn> calledMethodList = super.getCalledMethods(Method.INCOMING_DATA);
		Stream<PartialStream> retVal = calledMethodList.map(p -> (PartialStream)p.getArgs()[0]);
		return retVal.collect(Collectors.toList());
	}
	
	public PartialStream getSingleFrame() {
		List<PartialStream> frames = getFrames();
		if(frames.size() != 1)
			throw new IllegalStateException("There is not exactly one return value like expected.  num times method called="+frames.size());
		return frames.get(0);
	}

}
