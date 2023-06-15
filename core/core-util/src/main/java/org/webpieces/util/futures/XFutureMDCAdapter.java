package org.webpieces.util.futures;

import ch.qos.logback.classic.util.LogbackMDCAdapter;
import org.webpieces.util.context.Context;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class XFutureMDCAdapter extends LogbackMDCAdapter {

    //then we don't have to construct/gc all the time ...
    private static final String MDC_KEY = "webpieces.logback.async.mdc";
    private static final String MDC_DEQUE_KEY = "webpieces.logback.async.mdc.deque";
    private static final Map<String, String> DEFAULT_EMPTY = new HashMap<>();
    private static final Map<String, Deque<String>> DEFAULT_DEQUE_EMPTY = new HashMap<>();

    @Override
    public void put(String key, String val) {
        Map<String, String> map = fetchOrCreate();
        map.put(key, val);
    }

    @Override
    public String get(String key) {
        Map<String, String> map = fetchOrUseEmptyMap();
        return map.get(key);
    }



    @Override
    public void remove(String key) {
        Map<String, String> map = fetchOrUseEmptyMap();
        map.remove(key);
    }

    @Override
    public void clear() {
        Map<String, String> map = fetchOrUseEmptyMap();
        map.clear();
    }

    @Override
    public Map<String, String> getCopyOfContextMap() {
        Map<String, String> map = fetchOrUseEmptyMap();
        Map<String, String> copy = new HashMap<>(map);
        return copy;
    }

    @Override
    public void setContextMap(Map contextMap) {
        Context.put(MDC_KEY, contextMap);
    }

    @Override
    public void pushByKey(String key, String value) {
        if (key == null)
            return;

        Map<String, Deque<String>> stringDequeMap = fetchOrCreateDeque();
        Deque<String> strings = stringDequeMap.get(key);
        if(strings == null) {
            strings = new ArrayDeque<>();
            stringDequeMap.put(key, strings);
        }

        strings.add(value);
    }

    @Override
    public String popByKey(String key) {
        if (key == null)
            return null;

        Map<String, Deque<String>> stringDequeMap = fetchOrUseEmptyMapDeque();
        Deque<String> deque = stringDequeMap.get(key);
        if (deque == null)
            return null;
        return deque.pop();
    }

    @Override
    public Deque<String> getCopyOfDequeByKey(String key) {
        if (key == null)
            return null;

        Map<String, Deque<String>> stringDequeMap = fetchOrUseEmptyMapDeque();
        Deque<String> deque = stringDequeMap.get(key);
        if (deque == null)
            return null;

        return new ArrayDeque<String>(deque);
    }

    @Override
    public void clearDequeByKey(String key) {
        if (key == null)
            return;

        Map<String, Deque<String>> stringDequeMap = fetchOrUseEmptyMapDeque();
        Deque<String> deque = stringDequeMap.get(key);
        if (deque == null)
            return;
        deque.clear();
    }

    public Map<String, Deque<String>> fetchOrCreateDeque() {
        Map<String, Deque<String>> map = Context.get(MDC_DEQUE_KEY);
        if(map == null) {
            map = new HashMap<>();
            Context.put(MDC_DEQUE_KEY, map);
        }

        return map;
    }

    private Map<String, Deque<String>> fetchOrUseEmptyMapDeque() {
        Map<String, Deque<String>> map = Context.get(MDC_KEY);
        if(map == null)
            return DEFAULT_DEQUE_EMPTY;

        return map;
    }

    private Map<String, String> fetchOrCreate() {
        Map<String, String> map = Context.get(MDC_KEY);
        if(map == null) {
            map = new HashMap<>();
            Context.put(MDC_KEY, map);
        }

        return map;
    }

    private Map<String, String> fetchOrUseEmptyMap() {
        Map<String, String> map = Context.get(MDC_KEY);
        if(map == null)
            return DEFAULT_EMPTY;

        return map;
    }
}
