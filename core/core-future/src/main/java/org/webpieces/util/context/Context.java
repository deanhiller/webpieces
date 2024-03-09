package org.webpieces.util.context;

import java.util.*;

public class Context {

    public static final String HEADERS = "__headers";
    public static final String REQUEST = "__request";
    public static final String MDC_KEY = "webpieces.logback.async.mdc";

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
                if(header.getLoggerMDCKey() != existingFromHeader.getLoggerMDCKey())
                    throw new IllegalStateException("header="+tuple(header)+" and header="+tuple(existingFromHeader)+" define the same header " +
                            "but they define getLoggerMDCKey differently.  remove one of the plugins or modules to remove one of these headers or redine th header to match");
                compareHeader(header, existingFromHeader);
                continue; // no need to add duplicate, they are the same
            } else if(existingFromMdc != null) {
                if(header.getHeaderName() != existingFromMdc.getHeaderName())
                    throw new IllegalStateException("header="+tuple(header)+" and header="+tuple(existingFromMdc)+" define the same mdc key " +
                            "but they define getHeaderName() differently.  remove one of the plugins or modules to remove one of these headers or redine th header to match");
                compareHeader(header, existingFromMdc);

                continue; //no need to add duplicate, they are the same
            }

            headerKeyToHeader.put(header.getHeaderName(), header);
            mdcKeyToHeader.put(header.getLoggerMDCKey(), header);
        }
    }

    private static void compareHeader(PlatformHeaders header, PlatformHeaders existingFromHeader) {
        if(header.isWantLogged() != existingFromHeader.isWantLogged()
            || header.isDimensionForMetrics() != existingFromHeader.isDimensionForMetrics()
            || header.isSecured() != existingFromHeader.isSecured()
            || header.isWantTransferred() != existingFromHeader.isWantTransferred()
        )
            throw new IllegalStateException("header="+tuple(header)+" and header="+tuple(existingFromHeader)+" define the same header " +
                    "but they define their properties differently.  remove one of the plugins or modules to remove one of these headers or redine th header to match");
    }

    private static String tuple(PlatformHeaders header) {
        return header.getClass()+"."+header;
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
