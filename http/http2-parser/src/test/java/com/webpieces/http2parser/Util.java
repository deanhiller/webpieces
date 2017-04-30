package com.webpieces.http2parser;

import javax.xml.bind.DatatypeConverter;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

public class Util {
    private static DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    public static DataWrapper hexToBytes(String s) {
        byte[] data = DatatypeConverter.parseHexBinary(s.replaceAll("\\s+",""));
        return dataGen.wrapByteArray(data);
    }

	public static String toHexString(byte[] array) {
	    return DatatypeConverter.printHexBinary(array);
	}

}
