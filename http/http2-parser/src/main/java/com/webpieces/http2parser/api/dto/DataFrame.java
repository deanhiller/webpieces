package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.Padding;
import com.webpieces.http2parser.api.PaddingFactory;
import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2FrameType;

public class DataFrame extends AbstractHttp2Frame {

    @Override
    public Http2FrameType getFrameType() {
        return Http2FrameType.DATA;
    }

    /* flags */
    private boolean endStream = false; /* 0x1 */
    //private boolean padded = false;    /* 0x8 */

    public boolean isEndStream() {
        return endStream;
    }

    public void setEndStream(boolean endStream) {
        this.endStream = endStream;
    }

    /* payload */
    private DataWrapper data = dataGen.emptyWrapper();
    private Padding padding = PaddingFactory.createPadding();

    public Padding getPadding() {
        return padding;
    }

    public DataWrapper getData() {
        return data;
    }

    public void setData(DataWrapper data) {
        this.data = data;
    }

    public void setPadding(byte[] padding) {
        this.padding.setPadding(padding);
    }

    @Override
    public String toString() {
        return "DataFrame{" +
        		"streamId=" + super.toString() +
                ", endStream=" + endStream +
                ", data.len=" + data.getReadableSize() +
                ", padding=" + padding +
                "} ";
    }
}
