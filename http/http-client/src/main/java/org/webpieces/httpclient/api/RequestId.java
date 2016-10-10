package org.webpieces.httpclient.api;


public class RequestId {
    private Integer value;

    public RequestId(int value) {
        this.value = new Integer(value);
    }

    public RequestId(String s) throws NumberFormatException {
        this.value = new Integer(s);
    }

    public Integer getValue() {
        return value;
    }
}
