package com.webpieces.http2parser.api.dto;

import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2FrameType;
import com.webpieces.http2parser.api.dto.lib.PriorityDetails;

public class PriorityFrame extends AbstractHttp2Frame {

    /* flags */

    /* payload */
    private PriorityDetails priorityDetails = new PriorityDetails();

    public PriorityDetails getPriorityDetails() {
        return priorityDetails;
    }

    public void setPriorityDetails(PriorityDetails priorityDetails) {
        this.priorityDetails = priorityDetails;
    }
    
    @Override
    public Http2FrameType getFrameType() {
        return Http2FrameType.PRIORITY;
    }
    
    @Override
    public String toString() {
        return "PriorityFrame{" +
        		super.toString() +
                "priorityDetails=" + priorityDetails +
                "} ";
    }

}
