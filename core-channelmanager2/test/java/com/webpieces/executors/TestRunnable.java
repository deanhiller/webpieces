package com.webpieces.executors;

import com.webpieces.executors.SessionRunnable;

public class TestRunnable implements SessionRunnable {


    private String sessionId;
    private boolean wasRun;

    public TestRunnable(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public void run() {
        wasRun = true;
    }

    public boolean isWasRun() {
        return wasRun;
    }
}
