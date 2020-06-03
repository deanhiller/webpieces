package com.webpieces.http2.api.dto.lowlevel.lib;

import org.webpieces.data.api.DataWrapper;

public interface HasHeaderFragment extends Http2Frame {
    boolean isEndHeaders();

    void setEndHeaders(boolean endHeaders);

    DataWrapper getHeaderFragment();
    void setHeaderFragment(DataWrapper fragment);

}
