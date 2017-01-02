package com.webpieces.http2parser.api.dto.lib;

import java.util.List;

// These calls can only be used on headers that have been created by the parser out of multiple
// real header frames. It's kind of a 'fake' frame at that point.
@Deprecated
public interface HasHeaderList {
	@Deprecated
    void setHeaderList(List<Http2Header> headerList);
	@Deprecated
    List<Http2Header> getHeaderList();
}
