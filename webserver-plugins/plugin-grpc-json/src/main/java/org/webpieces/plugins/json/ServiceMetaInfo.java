package org.webpieces.plugins.json;

public class ServiceMetaInfo {

	private String controller;
	private String grpcService;

	public ServiceMetaInfo(String controller, String grpcService) {
		this.controller = controller;
		this.grpcService = grpcService;
	}

	public String getController() {
		return controller;
	}

	public String getGrpcService() {
		return grpcService;
	}
	
}
