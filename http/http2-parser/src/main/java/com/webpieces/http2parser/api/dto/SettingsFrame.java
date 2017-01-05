package com.webpieces.http2parser.api.dto;

import java.util.ArrayList;
import java.util.List;

import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2FrameType;
import com.webpieces.http2parser.api.dto.lib.Http2Setting;

public class SettingsFrame extends AbstractHttp2Frame {
    /* flags */
    private boolean ack = false; /* 0x1 */

    /* payload */
    // id 16bits
    // value 32bits
    //settings in the spec are ordered and are supposed to be applied in-order.  
    //above this layer we apply them all at one time but others could apply them in-order
    private List<Http2Setting> settings = new ArrayList<Http2Setting>();
    
    public SettingsFrame() {
    }
    public SettingsFrame(boolean b) {
    	ack = true;
	}

	@Override
    public Http2FrameType getFrameType() {
        return Http2FrameType.SETTINGS;
    }

    public boolean isAck() {
        return ack;
    }

    public void setAck(boolean ack) {
        this.ack = ack;
    }

    public void addSetting(Http2Setting setting) {
    	settings.add(setting);
    }

    public  List<Http2Setting> getSettings() {
    	return settings;
    }

	public void setSettings(List<Http2Setting> settings2) {
		this.settings = settings2;
	}
	
	@Override
    public String toString() {
        return "SettingsFrame{" +
        		super.toString() +
                ", ack=" + ack +
                ", settings=" + settings +"} ";
    }

}
