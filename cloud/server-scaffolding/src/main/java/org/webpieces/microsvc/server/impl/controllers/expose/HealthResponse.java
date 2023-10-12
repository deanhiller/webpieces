package org.webpieces.microsvc.server.impl.controllers.expose;

public class HealthResponse {
    private String status = "OK";

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
