package com.webpieces.http2parser;

import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2ParserFactory;
import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.ParserResult;
import org.junit.Assert;
import org.webpieces.data.api.*;

import javax.xml.bind.DatatypeConverter;
import javax.xml.crypto.Data;
import java.util.Base64;

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

    public static Http2Frame frameFromHex(String frameHex) {
        ParserResult result = parser.parse(dataWrapperFromHex(frameHex), dataGen.emptyWrapper());
        return result.getParsedFrames().get(0);
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
        String hex = frameToHex(frame);
        byte[] bytes = frameToBytes(frame);
        Assert.assertArrayEquals(bytes, toByteArray(hexFrame));
    }

}
