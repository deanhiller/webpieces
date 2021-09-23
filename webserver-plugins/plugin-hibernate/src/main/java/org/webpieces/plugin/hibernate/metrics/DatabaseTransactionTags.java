package org.webpieces.plugin.hibernate.metrics;

import java.util.HashMap;
import java.util.Map;

public class DatabaseTransactionTags {

    public static final String TRANSACTION = "transaction";

    private final String transaction;

    public DatabaseTransactionTags(String transaction) {
        this.transaction = transaction;
    }

    public String getTransaction() {
        return transaction;
    }

    public Map<String, String> getTagsMap() {
        Map<String, String> tags = new HashMap<>();
        tags.put(TRANSACTION, transaction);
        return tags;
    }

}
