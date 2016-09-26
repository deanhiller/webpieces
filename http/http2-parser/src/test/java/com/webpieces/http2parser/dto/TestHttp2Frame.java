package com.webpieces.http2parser.dto;

import dto.Http2Data;
import dto.Http2Frame;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

import java.util.Base64;

import javax.xml.bind.DatatypeConverter;

public class TestHttp2Frame {
    private static Base64.Encoder encoder = Base64.getEncoder();
    private static Base64.Decoder decoder = Base64.getDecoder();
    private static DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    private static String dataWrapperToBase64(DataWrapper data) {
        return encoder.encodeToString(data.createByteArray());
    }

    private static DataWrapper base64ToDataWrapper(String base64) {
        return dataGen.wrapByteArray(decoder.decode(base64));
    }


    private static String toHexString(byte[] array) {
        return DatatypeConverter.printHexBinary(array);
    }

    private static byte[] toByteArray(String s) {
        return DatatypeConverter.parseHexBinary(s.replaceAll("\\s+",""));
    }

    @Test
    public void testParseUnpaddedData() {
        // 24 bits length - 0x000008

        // 8 bits type - DATA - 0x01

        // 8 bits flags - none - 0x00

        // 1 bit reserved
        // 31 bits stream identifier - 0x00000001

        // payload - 8 bytes of FF

        String dataFrameHex = "00 00 08" + // Length
                           "00" + // Type
                           "00" + // Flags
                           "00 00 00 01" + // R + streamid
                           "FF FF FF FF FF FF FF FF"; // payload
        DataWrapper dataFrameDW = dataGen.wrapByteArray(toByteArray(dataFrameHex));
        Http2Frame frame = Http2Frame.createFromDataWrapper(dataFrameDW);
        Assert.assertTrue(Http2Data.class.isInstance(frame));

        Http2Data castFrame = (Http2Data) frame;

        byte[] data = castFrame.getData().createByteArray();
        Assert.assertArrayEquals(data, toByteArray("FF FF FF FF FF FF FF FF"));
        Assert.assertFalse(castFrame.isEndStream());
    }
}
