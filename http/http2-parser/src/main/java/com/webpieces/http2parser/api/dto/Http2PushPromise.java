package com.webpieces.http2parser.api.dto;

import com.webpieces.http2parser.api.HeaderBlock;
import com.webpieces.http2parser.api.HeaderBlockFactory;
import com.webpieces.http2parser.api.Padding;
import com.webpieces.http2parser.api.PaddingFactory;
import org.webpieces.data.api.DataWrapper;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public class Http2PushPromise extends Http2Frame {

    public Http2FrameType getFrameType() {
        return Http2FrameType.PUSH_PROMISE;
    }

    /* flags */
    private boolean endHeaders = false; /* 0x4 */
    //private boolean padded = false; /* 0x8 */

    public boolean isEndHeaders() {
        return endHeaders;
    }

    public void setEndHeaders(boolean endHeaders) {
        this.endHeaders = endHeaders;
    }

    /* payload */
    // reserved - 1bit
    private int promisedStreamId = 0x0; //31bits
    private HeaderBlock headerBlock = HeaderBlockFactory.createHeaderBlock();
    private Padding padding = PaddingFactory.createPadding();

    public void setPadding(byte[] padding) {
        this.padding.setPadding(padding);
    }

    public Padding getPadding() {
        return padding;
    }

    public int getPromisedStreamId() {
        return promisedStreamId;
    }

    public void setPromisedStreamId(int promisedStreamId) {
        this.promisedStreamId = promisedStreamId & 0x7FFFFFFF;
    }

    // Should reuse code in Http2HeadersImpl but multiple-inheritance is not possible?
    public void setHeaders(Map<String, String> headers) {
        headerBlock.setFromMap(headers);
    }

    public HeaderBlock getHeaderBlock() {
        return headerBlock;
    }

    public Map<String, String> getHeaders() {
        return headerBlock.getMap();
    }


}
