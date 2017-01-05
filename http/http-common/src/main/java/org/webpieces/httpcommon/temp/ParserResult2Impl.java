package org.webpieces.httpcommon.temp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.Http2Memento;
import com.webpieces.http2parser.api.dto.lib.HasHeaderFragment;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Setting;

public class ParserResult2Impl implements Http2Memento {

    /**
     * Only the proxy layer is using this!!!  ie. this is dead code eventually once I can kill it as it
     * exists elsewhere.  I want to keep the parser dead simple and don't want this piece in there.
     */
    @Deprecated
    private List<HasHeaderFragment> hasHeaderFragmentList = new LinkedList<>();
	private Http2Memento result;
	private List<Http2Frame> parsedFrames = new ArrayList<>();

	public ParserResult2Impl(Http2Memento result) {
		this.result = result;
	}

	public Http2Memento getResult() {
		return result;
	}

	public List<HasHeaderFragment> getHasHeaderFragmentList() {
		return hasHeaderFragmentList;
	}

	public void setHasHeaderFragmentList(List<HasHeaderFragment> hasHeaderFragmentList) {
		this.hasHeaderFragmentList = hasHeaderFragmentList;
	}

	@Override
	public List<Http2Frame> getParsedFrames() {
		return parsedFrames;
	}

	@Override
	public DataWrapper getLeftOverData() {
		return result.getLeftOverData();
	}

}
