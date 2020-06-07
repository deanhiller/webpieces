package org.webpieces.plugin.grpcjson;

import java.util.ArrayList;
import java.util.List;

public class GrpcJsonConfig {

	private int filterApplyLevel = 0;
	private String baseUrl;
	private List<ServiceMetaInfo> services = new ArrayList<>();

	@SuppressWarnings("rawtypes")
	public GrpcJsonConfig(String baseUrl, Class controller, Class grpcService) {
		this.baseUrl = baseUrl;
		addService(controller, grpcService);
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	@SuppressWarnings("rawtypes")
	public void addService(Class controller, Class grpcService) {
		if(!grpcService.getSimpleName().endsWith("Grpc"))
			throw new IllegalArgumentException("grpcService should end with Grpc");
		services.add(new ServiceMetaInfo(controller, grpcService));
	}

	public List<ServiceMetaInfo> getServices() {
		return services;
	}

	public int getFilterApplyLevel() {
		return filterApplyLevel;
	}

	public void setFilterApplyLevel(int filterApplyLevel) {
		this.filterApplyLevel = filterApplyLevel;
	}
	
}
