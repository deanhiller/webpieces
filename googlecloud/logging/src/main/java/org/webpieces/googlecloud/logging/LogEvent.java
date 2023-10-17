package org.webpieces.googlecloud.logging;

import java.util.HashMap;
import java.util.Map;

public class LogEvent {

    private String message;
    private boolean hasException;
    private long timestampMillis;
    private GCPCloudLoggingTimestamp timestamp;
    private String severity;
    private String thread;
    private String logger;
    private String socket;
    private String clientSocket;
    private String transactionId;
    private String version;
    private String instanceId;

    private Map<String, String> headers = new HashMap<>();

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isHasException() {
        return hasException;
    }

    public void setHasException(boolean hasException) {
        this.hasException = hasException;
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }

    public void setTimestampMillis(long timestampMillis) {
        this.timestampMillis = timestampMillis;
    }

    public GCPCloudLoggingTimestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(GCPCloudLoggingTimestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getThread() {
        return thread;
    }

    public void setThread(String thread) {
        this.thread = thread;
    }

    public String getLogger() {
        return logger;
    }

    public void setLogger(String logger) {
        this.logger = logger;
    }

    public String getSocket() {
        return socket;
    }

    public void setSocket(String socket) {
        this.socket = socket;
    }

    public String getClientSocket() {
        return clientSocket;
    }

    public void setClientSocket(String clientSocket) {
        this.clientSocket = clientSocket;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
