package com.webpieces.http2parser.api;

import org.webpieces.data.api.DataWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Http2ParserImpl { //extends Http2Parser

    private Map<Class<?>, Function<Http2Frame, DataWrapper>> dtoToMarshaller = new HashMap<>();


    public Http2Frame unmarshal(DataWrapper frame) {
        return null;
    }

    public DataWrapper marshal(Http2Frame frame) {

        Function<Http2Frame, DataWrapper> marshaller = dtoToMarshaller.get(frame);
        if(marshaller == null)
            return null; //throw here

        return marshaller.apply(frame);
    }

    public DataWrapper parseData(Http2Frame frame) {
        Http2Data data = (Http2Data) frame;

//        Common common = getCommon(data);
//
//        NonCommon common = getFlags(data);

        return null;

    }

}
