package org.webpieces.httpcommon.api;


public class RequestId {
    private Integer value;

    public RequestId(int value) {
        this.value = value;
    }

    public RequestId(String s) throws NumberFormatException {
        this.value = new Integer(s);
    }

    public Integer getValue() {
        return value;
    }
}
