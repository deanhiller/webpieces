package org.webpieces.util.context;

public interface ContextKey {

    String name();

    default boolean addToMDC() {
        String key = getMDCKey();
        return (key != null && !key.isBlank());
    }

    String getMDCKey();

}
