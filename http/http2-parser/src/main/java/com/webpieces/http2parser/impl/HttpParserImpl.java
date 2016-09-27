package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.HttpParser;
import com.webpieces.http2parser.api.ParserResult;
import com.webpieces.http2parser.dto.Http2Frame;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

import java.util.ArrayList;
import java.util.List;

public class HttpParserImpl implements HttpParser {
    private static DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    public ParserResult prepareToParse() {
        return new ParserResultImpl(new ArrayList<>(), dataGen.emptyWrapper());
    }

    public ParserResult parse(DataWrapper oldData, DataWrapper newData) {
        DataWrapper wrapperToParse;
        List<Http2Frame> frames = new ArrayList<>();

        if(oldData.getReadableSize() > 0)
            wrapperToParse = dataGen.chainDataWrappers(oldData, newData);
        else
            wrapperToParse = newData;

        // Loop until a return (ack)
        while(true) {
            int lengthOfData = wrapperToParse.getReadableSize();
            if (lengthOfData <= 3) {
                // Not even a length
                return new ParserResultImpl(frames, wrapperToParse);
            } else {
                // peek for length
                int length = Http2Frame.peekLengthOfFrame(wrapperToParse);
                if (lengthOfData < length) {
                    // not a whole frame
                    return new ParserResultImpl(frames, wrapperToParse);
                } else {
                    // parse a single frame, look for more
                    List<? extends DataWrapper> split = dataGen.split(wrapperToParse, length);
                    Http2Frame frame = Http2Frame.setFromDataWrapper(split.get(0));
                    frames.add(frame);
                    wrapperToParse = split.get(1);
                }
            }
        }
    }
}
