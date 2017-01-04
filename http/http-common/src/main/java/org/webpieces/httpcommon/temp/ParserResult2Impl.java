package org.webpieces.httpcommon.temp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.ParserResult;
import com.webpieces.http2parser.api.dto.lib.HasHeaderFragment;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Setting;

public class ParserResult2Impl implements ParserResult {

    /**
     * Only the proxy layer is using this!!!  ie. this is dead code eventually once I can kill it as it
     * exists elsewhere.  I want to keep the parser dead simple and don't want this piece in there.
     */
    @Deprecated
    private List<HasHeaderFragment> hasHeaderFragmentList = new LinkedList<>();
	private ParserResult result;
	private List<Http2Frame> parsedFrames = new ArrayList<>();

	public ParserResult2Impl(ParserResult result) {
		this.result = result;
	}

	public ParserResult getResult() {
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
	public DataWrapper getMoreData() {
		return result.getMoreData();
	}

}
