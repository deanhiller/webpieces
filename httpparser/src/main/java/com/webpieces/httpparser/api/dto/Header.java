package com.webpieces.httpparser.api.dto;

public class Header {

	private String name;
	private String value;
	
	public void setName(KnownHeaderName name) {
		this.name = name.getHeaderName();
	}
	
	/**
	 * Returns null if name is not a known one in the spec or returns the
	 * known name
	 * 
	 * @return
	 */
	public KnownHeaderName getKnownName() {
		return KnownHeaderName.lookup(name);
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
		Header other = (Header) obj;
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
		return name + " : " + value + "\r\n";
	}
	
	
}
