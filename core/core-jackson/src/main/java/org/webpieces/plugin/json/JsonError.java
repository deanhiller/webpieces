package org.webpieces.plugin.json;

import java.util.ArrayList;
import java.util.List;

public class JsonError {
    private String error;
    private int code;

    private String serviceWithError;

    private List<String> serviceFailureChain = new ArrayList<>();

    public String getError() {
        return error;
    }
    public void setError(String error) {
        this.error = error;
    }
    public int getCode() {
        return code;
    }
    public void setCode(int code) {
        this.code = code;
    }

    public String getServiceWithError() {
        return serviceWithError;
    }

    public void setServiceWithError(String serviceWithError) {
        this.serviceWithError = serviceWithError;
    }

    public List<String> getServiceFailureChain() {
        return serviceFailureChain;
    }

    public void setServiceFailureChain(List<String> serviceFailureChain) {
        this.serviceFailureChain = serviceFailureChain;
    }

    @Override
    public String toString() {
        return "JsonError{" +
                "error='" + error + '\'' +
                ", code=" + code +
                ", serviceWithError='" + serviceWithError + '\'' +
                ", serviceFailureChain=" + serviceFailureChain +
                '}';
    }
}
