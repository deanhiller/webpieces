package org.webpieces.util.cmdline2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Variables {

    //key to list of consumers of that key
    public Map<String, List<UsageHelp>> keyToAskedFor = new HashMap<>();
    public Map<String, ValueHolder<String>> mustMatchDefaults = new HashMap<>();
    public Map<String, ValueHolder<Object>> mustMatchTestDefaults = new HashMap<>();

    private Function<String, ValueHolder> fetchVar;

    public Variables(Function<String, ValueHolder> fetchVar) {
        this.fetchVar = fetchVar;
    }

    public ValueHolder<String> fetch(String varName) {
        return fetchVar.apply(varName);
    }
}
