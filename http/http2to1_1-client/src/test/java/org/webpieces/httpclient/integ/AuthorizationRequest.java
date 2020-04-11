package org.webpieces.httpclient.integ;

public class AuthorizationRequest {

	private String clientId;
	private String serviceId;
	private String token;

	
	//optional and only when clientId="internal"
	private String onBehalfOfId;

	public String getServiceId() {
		return serviceId;
	}
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getOnBehalfOfId() {
		return onBehalfOfId;
	}
	public void setOnBehalfOfId(String onBehalfOfId) {
		this.onBehalfOfId = onBehalfOfId;
	}
	
}
