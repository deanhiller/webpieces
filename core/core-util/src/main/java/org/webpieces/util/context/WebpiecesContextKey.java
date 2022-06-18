package org.webpieces.util.context;

public enum WebpiecesContextKey implements ContextKey {

    REQUEST_PATH;

    private final String mdcKey;

    WebpiecesContextKey() {
        this(null);
    }

    WebpiecesContextKey(String mdcKey) {
        this.mdcKey = mdcKey;
    }

    @Override
    public String getMDCKey() {
        return mdcKey;
    }

}
