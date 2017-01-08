package com.webpieces.http2parser;

import javax.xml.bind.DatatypeConverter;

import org.junit.Assert;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

import com.webpieces.http2parser.api.Http2Memento;
import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2ParserFactory;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;

public class UtilsForTest {
    private static DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
    static Http2Parser parser = Http2ParserFactory.createParser(new BufferCreationPool());

    public static String toHexString(byte[] array) {
        return DatatypeConverter.printHexBinary(array);
    }

    public static DataWrapper toDataWrapper(String s) {
    	return dataGen.wrapByteArray(toByteArray(s));
    }
    public static byte[] toByteArray(String s) {
        return DatatypeConverter.parseHexBinary(s.replaceAll("\\s+",""));
    }

    public static DataWrapper dataWrapperFromHex(String hex) {
        return dataGen.wrapByteArray(toByteArray(hex));
    }

    public static Http2Frame frameFromHex(String frameHex) {
    	DataWrapper data = dataWrapperFromHex(frameHex);
    	Http2Memento state = parser.prepareToParse(Integer.MAX_VALUE);
    	state = parser.parse(state, data);
    	return state.getParsedFrames().get(0);
    }

    public static DataWrapper frameToDataWrapper(Http2Frame frame) {
        return parser.marshal(frame);
    }

    public static byte[] frameToBytes(Http2Frame frame) {
        return frameToDataWrapper(frame).createByteArray();
    }

    public static String frameToHex(Http2Frame frame) {
        return toHexString(frameToBytes(frame));
    }

    public static boolean isReservedBitZero(DataWrapper frame) {
        byte b = frame.readByteAt(6);
        return (b & 0x80) == 0x00;
    }

    public static void testBidiFromBytes(String hexFrame) {
        Http2Frame frame = frameFromHex(hexFrame);
        //String hex = frameToHex(frame);
        byte[] bytes = frameToBytes(frame);
        Assert.assertArrayEquals(bytes, toByteArray(hexFrame));
    }

}
