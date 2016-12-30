package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;

public interface HasHeaderFragment extends Http2Frame {
    boolean isEndHeaders();

    void setEndHeaders(boolean endHeaders);

    DataWrapper getHeaderFragment();
    void setHeaderFragment(DataWrapper fragment);

}
