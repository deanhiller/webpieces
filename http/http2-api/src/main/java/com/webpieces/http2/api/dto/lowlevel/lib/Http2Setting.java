package com.webpieces.http2.api.dto.lowlevel.lib;

public class Http2Setting {

    // id unsigned 16bits
    // value unsigned 32bits 
	private int name; //java must use int for unsigned 16 bits
	private long value; //java must use long for unsigned 32 bits
	
	public Http2Setting(int name, long value) {
		this.name = name;
		this.value = value;
	}
	public Http2Setting(SettingsParameter name, long value) {
		this.name = name.getId();
		this.value = value;
	}
	
	public void setName(SettingsParameter name) {
		this.name = name.getId();
	}
	
	/**
	 * Returns null if name is not a known one in the spec or returns the
	 * known name
	 * 
	 * @return
	 */
	public SettingsParameter getKnownName() {
		return SettingsParameter.lookup(name);
	}
	
	public int getId() {
		return name;
	}
	public void setId(int key) {
		this.name = key;
	}
	public long getValue() {
		return value;
	}
	public void setValue(long value) {
		this.value = value;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + name;
		result = prime * result + (int) (value ^ (value >>> 32));
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
		Http2Setting other = (Http2Setting) obj;
		if (name != other.name)
			return false;
		if (value != other.value)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		String idAsStr;
		SettingsParameter knownName = getKnownName();
		if(knownName == null) {
			idAsStr = name+"";
		} else
			idAsStr = knownName+"";
		
		return "{"+idAsStr + ": " + value+"}";
	}
}
