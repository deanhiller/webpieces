package org.webpieces.httpparser.api.dto;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

public class HttpData extends HttpPayload {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private static final DataWrapper EMPTY_WRAPPER = dataGen.emptyWrapper();
	
	private DataWrapper body;
	private boolean isEndOfData;
	
	public HttpData() {
	}
	
	public HttpData(DataWrapper dataWrapper, boolean isEndOfData) {
		body = dataWrapper;
		this.isEndOfData = isEndOfData;
	}

	public boolean isEndOfData() {
		return isEndOfData;
	}

	public void setEndOfData(boolean isEndOfData) {
		this.isEndOfData = isEndOfData;
	}

	/**
	 * 
	 * @param data
	 */
	public void setBody(DataWrapper data) {
		this.body = data;
	}
	
	/**
	 * @return
	 */
	public DataWrapper getBody() { return body; }

	/**
	 *
	 */
	public void appendBody(DataWrapper data) {
		this.body = dataGen.chainDataWrappers(this.body, data);
	}
	
	/**
	 * convenience method for non-null body that will be 0 bytes if it was null
	 * @return
	 */
	public DataWrapper getBodyNonNull() {
		if(body == null)
			return EMPTY_WRAPPER;
		return body;
	}
	
	@Override
	public HttpMessageType getMessageType() {
		return HttpMessageType.DATA;
	}

}
