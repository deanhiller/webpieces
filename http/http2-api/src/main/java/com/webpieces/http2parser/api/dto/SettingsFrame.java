package com.webpieces.http2parser.api.dto;

import java.util.ArrayList;
import java.util.List;

import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2FrameType;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.Http2MsgType;
import com.webpieces.http2parser.api.dto.lib.Http2Setting;

public class SettingsFrame extends AbstractHttp2Frame implements Http2Msg {
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
    public SettingsFrame(boolean ack) {
    	this.ack = ack;
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
    public Http2FrameType getFrameType() {
        return Http2FrameType.SETTINGS;
    }
	@Override
	public Http2MsgType getMessageType() {
		return Http2MsgType.SETTINGS;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (ack ? 1231 : 1237);
		result = prime * result + ((settings == null) ? 0 : settings.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SettingsFrame other = (SettingsFrame) obj;
		if (ack != other.ack)
			return false;
		if (settings == null) {
			if (other.settings != null)
				return false;
		} else if (!settings.equals(other.settings))
			return false;
		return true;
	}
	
	@Override
    public String toString() {
        return "SettingsFrame{" +
        		super.toString() +
                ", ack=" + ack +
                ", settings=" + settings +"} ";
    }

}
