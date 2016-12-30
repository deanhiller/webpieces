package com.webpieces.http2parser.api.dto;

import java.util.LinkedList;

import com.webpieces.http2parser.api.dto.lib.Http2Header;

// These calls can only be used on headers that have been created by the parser out of multiple
// real header frames. It's kind of a 'fake' frame at that point.
public interface HasHeaderList {
    void setHeaderList(LinkedList<Http2Header> headerList);
    LinkedList<Http2Header> getHeaderList();
}
