package org.webpieces.microsvc.api;

import org.webpieces.util.context.Context;

import javax.naming.CompositeName;

public class Restore {
    private final String reqId;
    private final String prevReq;

    public Restore(String reqId, String prevReq) {
        this.reqId = reqId;
        this.prevReq = prevReq;
    }

    public void reset() {
        Context.putMagic(MicroSvcHeader.REQUEST_ID, reqId);
        if(prevReq == null) {
            Context.removeMagic(MicroSvcHeader.PREVIOUS_REQUEST_ID);
        } else {
            Context.putMagic(MicroSvcHeader.PREVIOUS_REQUEST_ID, prevReq);
        }
    }
}
