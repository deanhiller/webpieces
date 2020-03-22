package org.webpieces.elasticsearch.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class JsonDateDeserializer extends JsonDeserializer<ZonedDateTime> {

    @Override
    public ZonedDateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        long millis = jp.getLongValue();
        Instant instant = Instant.ofEpochMilli(millis);
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        return zdt;
    }
}
