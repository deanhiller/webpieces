package com.webpieces.http2parser;

import java.util.Base64;

import javax.xml.bind.DatatypeConverter;

import org.junit.Assert;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2ParserFactory;
import com.webpieces.http2parser.api.dto.AbstractHttp2Frame;

public class UtilsForTest {
    private static Base64.Encoder encoder = Base64.getEncoder();
    private static Base64.Decoder decoder = Base64.getDecoder();
    private static DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
    static Http2Parser parser = Http2ParserFactory.createParser(new BufferCreationPool());

    private static String dataWrapperToBase64(DataWrapper data) {
        return encoder.encodeToString(data.createByteArray());
    }

    private static DataWrapper base64ToDataWrapper(String base64) {
        return dataGen.wrapByteArray(decoder.decode(base64));
    }

    public static String toHexString(byte[] array) {
        return DatatypeConverter.printHexBinary(array);
    }

    public static byte[] toByteArray(String s) {
        return DatatypeConverter.parseHexBinary(s.replaceAll("\\s+",""));
    }

    public static DataWrapper dataWrapperFromHex(String hex) {
        return dataGen.wrapByteArray(toByteArray(hex));
    }

    public static AbstractHttp2Frame frameFromHex(String frameHex) {
        return parser.unmarshal(dataWrapperFromHex(frameHex));
    }

    public static DataWrapper frameToDataWrapper(AbstractHttp2Frame frame) {
        return parser.marshal(frame);
    }

    public static byte[] frameToBytes(AbstractHttp2Frame frame) {
        return frameToDataWrapper(frame).createByteArray();
    }

    public static String frameToHex(AbstractHttp2Frame frame) {
        return toHexString(frameToBytes(frame));
    }

    public static boolean isReservedBitZero(DataWrapper frame) {
        byte b = frame.readByteAt(6);
        return (b & 0x80) == 0x00;
    }

    public static void testBidiFromBytes(String hexFrame) {
        AbstractHttp2Frame frame = frameFromHex(hexFrame);
        String hex = frameToHex(frame);
        byte[] bytes = frameToBytes(frame);
        Assert.assertArrayEquals(bytes, toByteArray(hexFrame));
    }

}
