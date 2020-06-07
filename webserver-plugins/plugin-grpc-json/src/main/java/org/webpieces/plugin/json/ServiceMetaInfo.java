package org.webpieces.plugin.json;

@SuppressWarnings("rawtypes")
public class ServiceMetaInfo {

	private Class controller;
	private Class grpcService;

	public ServiceMetaInfo(Class controller2, Class grpcService2) {
		this.controller = controller2;
		this.grpcService = grpcService2;
	}

	public Class getController() {
		return controller;
	}

	public Class getGrpcService() {
		return grpcService;
	}
	
}
