package com.webpieces.http2parser.dto;

import dto.Http2Frame;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

import javax.xml.bind.DatatypeConverter;
import java.util.Base64;

class Util {
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

    static byte[] toByteArray(String s) {
        return DatatypeConverter.parseHexBinary(s.replaceAll("\\s+",""));
    }

    static DataWrapper dataWrapperFromHex(String hex) {
        return dataGen.wrapByteArray(toByteArray(hex));
    }

    static Http2Frame frameFromHex(String frameHex) {
        return Http2Frame.setFromDataWrapper(dataWrapperFromHex(frameHex));
    }

    static boolean isReservedBitZero(DataWrapper frame) {
        byte b = frame.readByteAt(6);
        return (b & 0x80) == 0x00;
    }

}
