package org.webpieces.httpparser.api.dto;

public class HttpRequestMethod {

	private String method;
	
	public HttpRequestMethod() {}
	
	public HttpRequestMethod(String method) {
		this.method = method;
	}
	public HttpRequestMethod(KnownHttpMethod knownMethod) {
		this.method = knownMethod.getCode();
	}
	
	public void setKnownStatus(KnownHttpMethod knownMethod) {
		method = knownMethod.getCode();
	}
	
	public String getMethodAsString() {
		return method;
	}
	
	public KnownHttpMethod getKnownStatus() {
		return KnownHttpMethod.lookup(method); 
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((method == null) ? 0 : method.hashCode());
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
		HttpRequestMethod other = (HttpRequestMethod) obj;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return method;
	}
	
}
