package com.webpieces.http2parser.impl;

import java.util.List;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

import com.webpieces.http2parser.api.dto.error.ConnectionException;
import com.webpieces.http2parser.api.dto.error.ParseFailReason;

public class PaddingUtil {
	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    public static DataSplit extractPayloadAndPadding(boolean isPadded, DataWrapper data, int streamId) {
    	if(!isPadded) {
    		return new DataSplit(data, DataWrapperGeneratorFactory.EMPTY);
    	}
    	
        short padLength = (short) (data.readByteAt(0) & 0xFF);
        if(padLength > data.getReadableSize()) {
            throw new ConnectionException(ParseFailReason.NOT_ENOUGH_PAD_DATA, streamId, 
            		"expected padLenth of="+padLength+" but only found="+data.getReadableSize());
        }
        List<? extends DataWrapper> split1 = dataGen.split(data, 1);
        List<? extends DataWrapper> split2 = dataGen.split(split1.get(1), split1.get(1).getReadableSize() - padLength);
        return new DataSplit(split2.get(0), split2.get(1));
    }
    
    public static DataWrapper padDataIfNeeded(DataWrapper data, DataWrapper padding) {
        if(padding.getReadableSize() > 0) {
            byte[] length = {(byte) padding.getReadableSize()};
            DataWrapper lengthDW = dataGen.wrapByteArray(length);
            return dataGen.chainDataWrappers(dataGen.chainDataWrappers(lengthDW, data), padding);
        } else
            return data;
    }
}
