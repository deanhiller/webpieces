package org.webpieces.httpcommon.api;

public class ResponseId {
    private Integer value;

    public ResponseId(int value) {
        this.value = value;
    }

    public ResponseId(String s) throws NumberFormatException {
        this.value = new Integer(s);
    }

    public Integer getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResponseId that = (ResponseId) o;

        return getValue() != null ? getValue().equals(that.getValue()) : that.getValue() == null;

    }

    @Override
    public int hashCode() {
        return getValue() != null ? getValue().hashCode() : 0;
    }
}
