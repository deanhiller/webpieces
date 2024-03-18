package org.webpieces.util.context;

import java.util.*;

public class Context {

    public static final String HEADERS = "__headers";
    public static final String REQUEST = "__request";
    public static final String MDC_KEY = "webpieces.logback.async.mdc";

    private static ThreadLocal<Map<String,Object>> context = ThreadLocal.withInitial(() -> new HashMap<>());

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

    /**
     * @deprecated
     */
    @Deprecated
    public static void restoreContext(Map<String, Object> props) {
        context.set(props);
    }

    public static void setContext(Map<String, Object> props) {
        context.set(props);
    }

    public static Map<String, Object> copyContext() {
        Map<String, Object> stringObjectMap = context.get();
        HashMap<String, Object> copiedMap = new HashMap<>(stringObjectMap);

        //special case as we need a deep copy of MDC if in use
        Map<String, String> mdc = (Map<String, String>) stringObjectMap.get(MDC_KEY);
        if(mdc != null) {
            Map<String, String> copiedMdc = new HashMap<>(mdc);
            copiedMap.put(MDC_KEY, copiedMdc);
        }

        Map<String, String> magicHeaders = get(HEADERS);
        if(magicHeaders != null) {
            Map<String, String> copiedHeaders = new HashMap<>(magicHeaders);
            copiedMap.put(HEADERS, copiedHeaders);
        }

        return copiedMap;
    }

    public static void putMagic(PlatformHeaders header, String value) {
        if(header.getHeaderName() != null) {
            Map<String, String> magicHeaders = get(HEADERS);
            if (magicHeaders == null) {
                magicHeaders = new HashMap<>();
                put(HEADERS, magicHeaders);
            }

            magicHeaders.put(header.getHeaderName(), value);
        }

        if(header.getLoggerMDCKey() != null) {
            Map<String, String> map = get(MDC_KEY);
            if (map == null) {
                map = new HashMap<>();
                put(MDC_KEY, map);
            }

            map.put(header.getLoggerMDCKey(), value);
        }
    }

    public static String getMagic(PlatformHeaders header) {
        if(header.getHeaderName() != null) {
            Map<String, String> magicHeaders = get(HEADERS);
            if (magicHeaders == null) {
                return null;
            }
            return magicHeaders.get(header.getHeaderName());
        }

        Map<String, String> map = get(MDC_KEY);
        if (map == null) {
            return null;
        }

        return map.get(header.getLoggerMDCKey());
    }

    public static void removeMagic(PlatformHeaders header) {
        if(header.getLoggerMDCKey() != null) {
            Map<String, String> map = get(MDC_KEY);
            if (map != null) {
                map.remove(header.getLoggerMDCKey());
            }
        }

        if(header.getHeaderName() != null) {
            Map<String, String> magicHeaders = get(HEADERS);
            if (magicHeaders != null) {
                magicHeaders.remove(header.getHeaderName());
                if (magicHeaders.isEmpty()) {
                    remove(HEADERS); //clean up
                }
            }
        }
    }

    public static void clearMagic() {
        put(HEADERS, null); //clean up
    }


}
