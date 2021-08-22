package org.webpieces.microsvc.client.impl;

import com.orderlyhealth.api.FeatureToggle;
import com.orderlyhealth.api.OrderlyHeaders;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RouterRequest;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class InternalAddHeaders implements AddHeaders {

    @Override
    public List<Http2Header> addHeaders(InetSocketAddress apiAddress) {
        
        String hostname = apiAddress.getHostName();

        if(!hostname.startsWith("staging-") && !hostname.endsWith("orderlyhealth.com")
            && !hostname.startsWith("localhost") && !hostname.startsWith("host.docker.internal")
            && !hostname.startsWith("demo-")) {
            throw new IllegalArgumentException("This method sendHttpRequest is only allowed for internal use because it contains"
                + "sensitive internal information. Address=" + apiAddress);
        }

        RouterRequest routerReq = Current.request();
        String clientId = (String)routerReq.requestState.get(OrderlyHeaders.CLIENT_ID.getHeaderName());
        String services = (String)routerReq.requestState.get(OrderlyHeaders.SERVICES.getHeaderName());

        if(clientId == null) {
            throw new IllegalStateException("If this is on a customer request path, perhaps you are missing a filter? If not, you need"
                + " to call routerRequest.requestState.put(\"clientId\") before calling any clients! If you don't have a clientId (this is rare),"
                + " talk to Dean first and perhaps we will in with 'orderly' in very special cases");
        }

        List<Http2Header> headers = new ArrayList<>();

        for(OrderlyHeaders header : OrderlyHeaders.values()) {

            if(!header.isPropagated()) {
                continue;
            }

            if(header == OrderlyHeaders.INTERNAL_SECURE_KEY) {
                continue;
            }

            Object value = Current.request().getRequestState(header.getHeaderName());

            if(value == null) {
                continue;
            }

            String str = String.valueOf(value);

            if(!str.isEmpty()) {
                headers.add(new Http2Header(header.getHeaderName(), str));
            }

        }

        String featureToggles = FeatureToggle.getValue();
        if(!featureToggles.isBlank()) {
            headers.add(new Http2Header(OrderlyHeaders.FEATURE_TOGGLE.getHeaderName(), featureToggles));
        }

        headers.add(new Http2Header(OrderlyHeaders.INTERNAL_SECURE_KEY.getHeaderName(), "2Xptz1r63xNEbOkVxvzEX5y9N"));

        return headers;

    }

}
