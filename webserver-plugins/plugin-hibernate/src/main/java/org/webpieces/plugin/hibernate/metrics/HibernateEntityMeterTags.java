package org.webpieces.plugin.hibernate.metrics;

import java.util.HashMap;
import java.util.Map;

public class HibernateEntityMeterTags {

    public static final String SERVICE = "service";
    public static final String ENTITY_NAME = "entity";
    public static final String REQUEST = "request";

    private final String service;
    private final String entity;
    private final String request;

    public HibernateEntityMeterTags(String service, String entity, String request) {
        this.service = service;
        this.entity = entity;
        this.request = request;
    }

    public String getService() {
        return service;
    }

    public String getEntity() {
        return entity;
    }

    public String getRequest() {
        return request;
    }

    public Map<String, String> getTagsMap() {
        Map<String, String> tags = new HashMap<>();
        tags.put(SERVICE, service);
        tags.put(ENTITY_NAME, entity);
        tags.put(REQUEST, request);
        return tags;
    }

}
