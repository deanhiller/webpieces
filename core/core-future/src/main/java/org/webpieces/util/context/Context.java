package org.webpieces.util.context;

import java.util.*;

public class Context {

    public static final String HEADERS = "__headers";
    public static final String REQUEST = "__request";

    private static ThreadLocal<Map<String,Object>> context = ThreadLocal.withInitial(() -> new HashMap<>());

    public static boolean wasDupsChecked = false;
    /**
     * We use header as the key unless null and then use mdc as the key.  Do not allow duplication
     * including mdc can't use a header name if used as the key.
     */
    public synchronized static void checkForDuplicates(List<PlatformHeaders> platformHeaders) {
        if(wasDupsChecked) //only check once 
            return;

        Map<String, PlatformHeaders> headerKeyToHeader = new HashMap<>();
        Map<String, PlatformHeaders> mdcKeyToHeader = new HashMap<>();

        for(PlatformHeaders header : platformHeaders) {
            if(header.getHeaderName() == null && header.getLoggerMDCKey() == null) {
                throw new IllegalArgumentException("Either header or MDC must contain a value.  both cannot be null");
            }
            PlatformHeaders existingFromHeader = null;
            if(header.getHeaderName() != null)
                existingFromHeader = headerKeyToHeader.get(header.getHeaderName());
            PlatformHeaders existingFromMdc = null;
            if(header.getLoggerMDCKey() != null)
                existingFromMdc = mdcKeyToHeader.get(header.getLoggerMDCKey());

            if(existingFromHeader != null) {
                throw new IllegalArgumentException("Duplicate in Platform headers not allowed. key="
                        +header.getHeaderName()+" exists in "+tuple(existingFromHeader)+" and in "+tuple(header));
            } else if(existingFromMdc != null) {
                throw new IllegalArgumentException("Duplicate in Platform headers not allowed. key="
                        +header.getHeaderName()+" exists in "+tuple(existingFromMdc)+" and in "+tuple(header));
            }

            headerKeyToHeader.put(header.getHeaderName(), header);
            mdcKeyToHeader.put(header.getLoggerMDCKey(), header);
        }
    }

    private static String tuple(PlatformHeaders header) {
        return header.getHeaderName()+"/"+header.getLoggerMDCKey();
    }

    public static <T> T get(String key) {
        return (T)context.get().get(key);
    }

    public static void put(String key, Object value) {
        context.get().put(key, value);
    }

    public static Object remove(String key) {
        return context.get().remove(key);
    }

    public static void clear() {
        context.remove();
    }

    public static Map<String, Object> getContext() {
        return context.get();
    }

    public static void restoreContext(Map<String, Object> props) {
        context.set(props);
    }

    public static Map<String, Object> copyContext() {
        Map<String, Object> stringObjectMap = context.get();
        return new HashMap<>(stringObjectMap);
    }

    public static void putMagic(PlatformHeaders header, String value) {
        Map<String, String> magicHeaders = get(HEADERS);
        if(magicHeaders == null) {
            magicHeaders = new HashMap<>();
            put(HEADERS, magicHeaders);
        }

        String key = findKey(header);
        magicHeaders.put(key, value);
    }

    private static String findKey(PlatformHeaders header) {
        String headerName = header.getHeaderName();
        if(headerName != null)
            return headerName;
        return header.getLoggerMDCKey();
    }

    public static String getMagic(PlatformHeaders header) {
        Map<String, String> magicHeaders = get(HEADERS);
        if(magicHeaders == null) {
            return null;
        }
        String key = findKey(header);
        return magicHeaders.get(key);
    }

    public static void removeMagic(PlatformHeaders header) {
        Map<String, String> magicHeaders = get(HEADERS);
        if(magicHeaders == null)
            return;

        String key = findKey(header);
        magicHeaders.remove(key);
        if(magicHeaders.size() == 0) {
            put(HEADERS, null); //clean up
        }
    }

    public static void clearMagic() {
        put(HEADERS, null); //clean up
    }


}
