package org.webpieces.recorder.impl;

public interface TestCaseRecorder {

    public static final String RECORDER_KEY = "recorder";

    public void addEndpointInfo(EndpointInfo info);

    public EndpointInfo getLastEndpointInfo();

}
