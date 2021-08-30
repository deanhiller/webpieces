package org.webpieces.plugin.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import javax.inject.Inject;
import javax.inject.Provider;

public class ObjectMapperProvider implements Provider<ObjectMapper> {

    private ConverterConfig config;

    @Inject
    public ObjectMapperProvider(ConverterConfig config) {
        this.config = config;
    }

    public ObjectMapper get() {
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .registerModule(new JavaTimeModule())
                .registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));

        if(config.isConvertNullToEmptyStr()) {
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.configOverride(String.class)
                    .setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
        }

        return mapper;
    }
}
