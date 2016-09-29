package com.webpieces.http2parser.api;

import org.webpieces.data.api.DataWrapper;

public interface Padding {
    void setPadding(byte[] padding);
    boolean isPadded();
    void setIsPadded(boolean isPadded);

    byte[] getPadding();
    DataWrapper extractPayloadAndSetPaddingIfNeeded(DataWrapper data);
    DataWrapper padDataIfNeeded(DataWrapper data);

}
