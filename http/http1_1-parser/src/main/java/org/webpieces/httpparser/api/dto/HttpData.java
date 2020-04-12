package org.webpieces.httpparser.api.dto;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

public class HttpData extends HttpPayload {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private static final DataWrapper EMPTY_WRAPPER = dataGen.emptyWrapper();
	
	private DataWrapper body;
	private boolean isEndOfData;
	private boolean isStartOfChunk;
	private boolean isEndOfChunk;
	
	public HttpData() {
	}
	
	public HttpData(DataWrapper dataWrapper, boolean isEndOfData) {
		body = dataWrapper;
		this.isEndOfData = isEndOfData;
	}

	/**
	 * Returns if this is the very last HttpData for this stream of HttpDatas
	 */
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

	public void setStartOfChunk(boolean isStartOfChunk) {
		this.isStartOfChunk = isStartOfChunk;
	}

	/**
	 * There can be many HttpData that represent ONE chunk OR one HttpData 
	 * representing a http Chunk.  This tells you if this is the first of an
	 * http chunk.  There are many chunks in a stream of data so isStart can be
	 * true for quite a few HttpData.  isEndofData() tells
	 * you if it is the last HttpData AND also the last HttpChunk or another
	 * words the last last part of the request or response.  The first
	 * HttpData you get is obviously the isFirstData so we don't have a 
	 * flag for that.
	 * 
	 * @return
	 */
	public boolean isStartOfChunk() {
		return isStartOfChunk;
	}

	public void setEndOfChunk(boolean isEndOfChunk) {
		this.isEndOfChunk = isEndOfChunk;
	}

	/**
	 * There can be many HttpData that represent ONE chunk OR one HttpData 
	 * representing a http Chunk.  This tells you if this is the last of that
	 * chunk.  There are many chunks in a stream of data.  isEndofData() tells
	 * you if it is the last HttpData AND also the last HttpChunk or another
	 * words the last last part of the request or response
	 * 
	 * @return
	 */
	public boolean isEndOfChunk() {
		return isEndOfChunk;
	}

}
