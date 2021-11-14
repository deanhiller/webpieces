package org.webpieces.microsvc.server.impl;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import org.webpieces.microsvc.impl.EndpointInfo;
import org.webpieces.microsvc.impl.TestCaseRecorder;
import org.webpieces.router.api.routes.MethodMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestCaseRecorderImpl implements TestCaseRecorder {
    private MethodMeta meta;
    private Map<String, Object> fullRequestContext;
    private Http2Request originalRequest;
    private List<EndpointInfo> endpointInfo = new ArrayList<>();

    public TestCaseRecorderImpl(Http2Request originalRequest, MethodMeta meta, Map<String, Object> fullRequestContext) {
        this.originalRequest = originalRequest;
        this.meta = meta;
        this.fullRequestContext = fullRequestContext;
    }

    public void addEndpointInfo(EndpointInfo info) {
        endpointInfo.add(info);
    }

    public void spitOutTestCase() {
        //1. print out the bootstrap of test case that extends base FeatureTest.java(all FeatureTest.java are the same)
        //2. print out creation of request to server
        //3. print out adding responses to mock objects AND checking if mock exists or not and creating mocks if needed
        //4. check if Requests.java exists and Response.java exists
        //5. call the client method endpoint
        //6. validate response from client method
        //7. validate requests received by mock objects
    }
}
