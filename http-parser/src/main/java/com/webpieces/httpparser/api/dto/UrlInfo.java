package com.webpieces.httpparser.api.dto;

public class UrlInfo {

	private String prefix;
	private String host;
	private Integer port;

	public UrlInfo() {}
	
	public UrlInfo(String prefix, String host, Integer port) {
		this.prefix = prefix;
		this.host = host;
		this.port = port;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getResolvedPort() {
		if(port == null) {
			if("https".equals(prefix))
				return 443;
			else if("http".equals(prefix))
				return 80;
			else
				return null;
		}
		return port;
	}

	public Integer getPort() {
		return port;
	}
	
	public void setPort(Integer port) {
		this.port = port;
	}
	
}
