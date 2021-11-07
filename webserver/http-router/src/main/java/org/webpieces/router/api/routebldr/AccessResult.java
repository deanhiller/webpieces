package org.webpieces.router.api.routebldr;

public class AccessResult {

    private final boolean isAllowed;
    private final String reasonForDenial;

    public AccessResult() {
        isAllowed = true;
        reasonForDenial = null;
    }

    public AccessResult(String reasonForDenial) {
        this.isAllowed = false;
        this.reasonForDenial = reasonForDenial;
    }

    public boolean isAllowed() {
        return isAllowed;
    }

    public String getReasonForDenial() {
        return reasonForDenial;
    }
}
