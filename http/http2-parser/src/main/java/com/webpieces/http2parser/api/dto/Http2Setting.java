package com.webpieces.http2parser.api.dto;

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
	public String toString() {
		return name + ": " + value + "\r\n";
	}
}
