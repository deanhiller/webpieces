package com.webpieces.http2.api.dto.lowlevel.lib;

public class Http2Header {
	private String name;
	private String value;
	
	public Http2Header() {
	}
	public Http2Header(String name, String value) {
		this.name = name;
		this.value = value;
	}
	public Http2Header(Http2HeaderName name, String value) {
		this.name = name.getHeaderName();
		this.value = value;
	}
	
	public void setName(Http2HeaderName name) {
		this.name = name.getHeaderName();
	}
	
	/**
	 * Returns null if name is not a known one in the spec or returns the
	 * known name
	 * 
	 * @return
	 */
	public Http2HeaderName getKnownName() {
		return Http2HeaderName.lookup(name);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String key) {
		this.name = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		Http2Header other = (Http2Header) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return name + ": " + value + "\r\n";
	}
}
