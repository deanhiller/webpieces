package com.webpieces.http2parser.api.dto;

import com.webpieces.http2parser.api.HeaderBlock;
import com.webpieces.http2parser.api.HeaderBlockFactory;
import org.webpieces.data.api.DataWrapper;

import java.util.Map;

public class Http2Continuation extends Http2Frame {

        public Http2FrameType getFrameType() {
            return Http2FrameType.CONTINUATION;
        }

        /* flags */
        private boolean endHeaders = false; /* 0x4 */

        public void unmarshalFlags(byte flags) {
            endHeaders = (flags & 0x4) == 0x4;
        }

        public boolean isEndHeaders() {
            return endHeaders;
        }

        public void setEndHeaders() {
            this.endHeaders = true;
        }

        /* payload */
        private HeaderBlock headerBlock = HeaderBlockFactory.create();

        public void unmarshalPayload(DataWrapper payload) {
            headerBlock.deserialize(payload);
        }

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
