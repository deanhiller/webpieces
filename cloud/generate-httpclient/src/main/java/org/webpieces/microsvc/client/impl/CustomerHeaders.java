package org.webpieces.microsvc.client.impl;

import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;

import javax.inject.Singleton;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class CustomerHeaders extends com.orderlyhealth.json.client.util.InternalAddHeaders {

    private final String token;

    public CustomerHeaders(String base64Token) {

        if(base64Token == null) {
            throw new IllegalArgumentException("base64Token is required");
        }

        this.token = base64Token;

    }

    @Override
    public List<Http2Header> addHeaders(InetSocketAddress apiAddress) {

        List<Http2Header> headers = new ArrayList<>();

        headers.add(new Http2Header("Authorization", "Basic " + token));

        return headers;

    }

}
