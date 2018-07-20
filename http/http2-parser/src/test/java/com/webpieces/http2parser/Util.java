package com.webpieces.http2parser;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.util.bytes.Hex;

public class Util {
    private static DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    public static DataWrapper hexToBytes(String s) {
        byte[] data = Hex.parseHexBinary(s.replaceAll("\\s+",""));
        return dataGen.wrapByteArray(data);
    }

	public static String toHexString(byte[] array) {
	    return Hex.printHexBinary(array);
	}


}
