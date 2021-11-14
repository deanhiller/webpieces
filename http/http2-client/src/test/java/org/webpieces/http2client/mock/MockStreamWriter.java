package org.webpieces.http2client.mock;

import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2.api.streaming.StreamWriter;
import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;

import java.util.List;
import org.webpieces.util.futures.XFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MockStreamWriter extends MockSuperclass implements StreamWriter {

	enum Method implements MethodEnum {
		INCOMING_DATA
	}
	
	public MockStreamWriter() {
		setDefaultReturnValue(Method.INCOMING_DATA, XFuture.completedFuture(null));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public XFuture<Void> processPiece(StreamMsg data) {
		return (XFuture<Void>) super.calledMethod(Method.INCOMING_DATA, data);
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

	public void addProcessResponse(XFuture<Void> future) {
		super.addValueToReturn(Method.INCOMING_DATA, future);
	}

}
