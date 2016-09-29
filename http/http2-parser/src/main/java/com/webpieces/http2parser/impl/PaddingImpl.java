package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.Padding;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import java.util.List;

public class PaddingImpl implements Padding {
    private byte[] padding = null;
    private boolean padded = false;

    final private DataWrapperGenerator dataGen;

    public PaddingImpl(DataWrapperGenerator dataGen) {
        this.dataGen = dataGen;
    }

    @Override
    public void setPadding(byte[] padding) {
        this.padding = padding;
        this.padded = true;
    }

    @Override
    public boolean isPadded() {
        return padded;
    }

    @Override
    public void setIsPadded(boolean isPadded) {
        this.padded = isPadded;
    }

    @Override
    public byte[] getPadding() {
        return padding;
    }

    @Override
    public DataWrapper extractPayloadAndSetPaddingIfNeeded(DataWrapper data) {
        if(isPadded()) {
            byte padLength = data.readByteAt(0);
            List<? extends DataWrapper> split1 = dataGen.split(data, 1);
            List<? extends DataWrapper> split2 = dataGen.split(split1.get(1), split1.get(1).getReadableSize() - padLength);
            setPadding(split2.get(1).createByteArray());
            return split2.get(0);
        }
        else
            return data;
    }

    @Override
    public DataWrapper padDataIfNeeded(DataWrapper data) {
        if(isPadded()) {
            byte[] length = {(byte) padding.length};
            DataWrapper lengthDW = dataGen.wrapByteArray(length);
            DataWrapper paddingDW = dataGen.wrapByteArray(padding);
            return dataGen.chainDataWrappers(dataGen.chainDataWrappers(lengthDW, data), paddingDW);
        } else
            return data;
    }
}
