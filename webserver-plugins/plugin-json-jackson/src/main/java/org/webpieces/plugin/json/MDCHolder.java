package org.webpieces.plugin.json;

import java.util.Map;

public class MDCHolder {
    private Map<String, String> mdcMap;

    public void setMDCMap(Map<String, String> mdcMap) {
        this.mdcMap = mdcMap;
    }

    public Map<String, String> getMDCMap() {
        return mdcMap;
    }

}
