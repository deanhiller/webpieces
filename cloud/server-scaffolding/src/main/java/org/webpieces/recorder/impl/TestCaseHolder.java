package org.webpieces.recorder.impl;

public class TestCaseHolder {

    private StringBuilder testCase = new StringBuilder();

    public void add(String s) {
        testCase.append(s);
    }

    public String toString() {
        return testCase.toString();
    }
}
