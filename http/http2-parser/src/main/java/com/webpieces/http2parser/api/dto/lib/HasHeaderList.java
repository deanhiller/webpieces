package com.webpieces.http2parser.api.dto.lib;

import java.util.LinkedList;

// These calls can only be used on headers that have been created by the parser out of multiple
// real header frames. It's kind of a 'fake' frame at that point.
public interface HasHeaderList {
    void setHeaderList(LinkedList<Http2Header> headerList);
    LinkedList<Http2Header> getHeaderList();
}
