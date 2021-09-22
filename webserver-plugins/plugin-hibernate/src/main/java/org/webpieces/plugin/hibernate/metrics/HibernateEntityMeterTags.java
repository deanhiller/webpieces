package org.webpieces.plugin.hibernate.metrics;

import java.util.HashMap;
import java.util.Map;

public class HibernateEntityMeterTags {

    public static final String ENTITY_NAME = "entity";
    public static final String REQUEST = "request";

    private final String entity;
    private final String request;

    public HibernateEntityMeterTags(String entity, String request) {
        this.entity = entity;
        this.request = request;
    }

    public String getEntity() {
        return entity;
    }

    public String getRequest() {
        return request;
    }

    public Map<String, String> getTagsMap() {
        Map<String, String> tags = new HashMap<>();
        tags.put(ENTITY_NAME, entity);
        tags.put(REQUEST, request);
        return tags;
    }

}
