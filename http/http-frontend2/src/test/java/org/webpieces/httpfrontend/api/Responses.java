package org.webpieces.httpfrontend.api;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.HttpResponseStatus;
import org.webpieces.httpparser.api.dto.HttpResponseStatusLine;
import org.webpieces.httpparser.api.dto.KnownStatusCode;

public class Responses {
    public static HttpResponse createResponse(KnownStatusCode status, DataWrapper body) {
        HttpResponse resp = new HttpResponse();
        HttpResponseStatusLine statusLine = new HttpResponseStatusLine();
        HttpResponseStatus statusCode = new HttpResponseStatus();
        statusCode.setKnownStatus(status);
        statusLine.setStatus(statusCode);
        resp.setStatusLine(statusLine);

        resp.setBody(body);
        resp.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, Integer.toString(body.getReadableSize())));
        return resp;
    }

    public static HttpResponse copyResponseExceptBody(HttpResponse response) {
        HttpResponse newResponse = new HttpResponse();
        newResponse.setStatusLine(response.getStatusLine());
        response.getHeaders().forEach(newResponse::addHeader);
        return newResponse;
    }
}
