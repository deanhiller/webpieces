package org.webpieces.microsvc.client.impl;

import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;

import java.net.InetSocketAddress;
import java.util.List;

public interface AddHeaders {

    /**
     * Add any auth or other headers you need with this method
     */
    List<Http2Header> addHeaders(InetSocketAddress apiAddress);

}
