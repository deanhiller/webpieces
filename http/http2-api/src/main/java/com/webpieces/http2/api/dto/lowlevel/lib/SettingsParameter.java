package com.webpieces.http2.api.dto.lowlevel.lib;

import java.util.HashMap;
import java.util.Map;

public enum SettingsParameter {
    SETTINGS_HEADER_TABLE_SIZE(0x1),
    SETTINGS_ENABLE_PUSH(0x2),
    SETTINGS_MAX_CONCURRENT_STREAMS(0x3),
    SETTINGS_INITIAL_WINDOW_SIZE(0x4),
    SETTINGS_MAX_FRAME_SIZE(0x5),
    SETTINGS_MAX_HEADER_LIST_SIZE(0x6);

	private static Map<Integer, SettingsParameter> lookup = new HashMap<>();
	
	static {
		for(SettingsParameter name : SettingsParameter.values()) {
			lookup.put(name.getId(), name);
		}
	}
	
    private int id; //java must use int for 16 unsigned bits (short is for 16 bit signed)

    SettingsParameter(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

	public static SettingsParameter lookup(int id) {
		return lookup.get(id);
	}
}