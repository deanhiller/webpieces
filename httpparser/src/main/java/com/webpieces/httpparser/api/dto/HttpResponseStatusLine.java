package com.webpieces.httpparser.api.dto;

public class HttpResponseStatusLine {

	private HttpVersion version;
	private HttpResponseStatus status;
	
	public HttpVersion getVersion() {
		return version;
	}
	public void setVersion(HttpVersion version) {
		this.version = version;
	}
	public HttpResponseStatus getStatus() {
		return status;
	}
	public void setStatus(HttpResponseStatus status) {
		this.status = status;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
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
		HttpResponseStatusLine other = (HttpResponseStatusLine) obj;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return version + " " + status.getCode() + " " + status.getReason() + "\r\n";
	}
	
}
