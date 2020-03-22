package org.webpieces.elasticsearch.util;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.ZonedDateTime;

public class JsonDateSerializer extends JsonSerializer<ZonedDateTime> {

    @Override
    public void serialize(ZonedDateTime value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        long millis = value.toInstant().toEpochMilli();
        jgen.writeNumber(millis);
    }
}
