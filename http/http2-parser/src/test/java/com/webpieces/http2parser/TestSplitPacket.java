package com.webpieces.http2parser;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.data.api.TwoPools;

import com.webpieces.http2parser.api.Http2Memento;
import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2ParserFactory;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class TestSplitPacket {
	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
    private static Http2Parser parser = Http2ParserFactory.createParser(new TwoPools("pl", new SimpleMeterRegistry()));

    static private String basicSettings() {
    	String data =
            "00 00 0C" + // length
            "04" +  // type
            "00" + //flags
            "00 00 00 00" + //R + streamid
            "00 02 00 00 00 01" + //setting 1 (enable push)
            "00 03 00 00 01 00"; //setting 2 (max streams)
        return data.replaceAll("\\s+","");
    }
    
	private Http2Memento memento;

    @Before
    public void setUp() {
    	memento = parser.prepareToParse(Long.MAX_VALUE);    	
    }
    
    @Test
    public void testParseSettings() {
    	DataWrapper data = Util.hexToBytes(basicSettings());
    	
    	int size = data.getReadableSize();
    	byte[] firstPart = data.readBytesAt(0, 10);
    	byte[] secondHalf = data.readBytesAt(10, size-10);
    	DataWrapper data1 = dataGen.wrapByteArray(firstPart);
    	DataWrapper data2 = dataGen.wrapByteArray(secondHalf);
    	
    	Http2Memento state = parser.parse(memento, data1);
    	Assert.assertEquals(0, state.getParsedFrames().size());
    	Assert.assertEquals(0, state.getNumBytesJustParsed());
    	
    	state = parser.parse(memento, data2);
    	Assert.assertEquals(1, state.getParsedFrames().size());
    	Assert.assertEquals(size, state.getNumBytesJustParsed());
    }
    
    
}
