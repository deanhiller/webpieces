package org.webpieces.plugin.hibernate.metrics;

import java.util.HashMap;
import java.util.Map;

public class DatabaseTransactionTags {

    public static final String REQUEST = "request";
    public static final String TRANSACTION = "transaction";

    private final String request;
    private final String transaction;

    public DatabaseTransactionTags(String request, String transaction) {
        this.request = request;
        this.transaction = transaction;
    }

    public String getRequest() {
        return request;
    }

    public String getTransaction() {
        return transaction;
    }

    public Map<String, String> getTagsMap() {
        Map<String, String> tags = new HashMap<>();
        tags.put(REQUEST, request);
        tags.put(TRANSACTION, transaction);
        return tags;
    }

}
