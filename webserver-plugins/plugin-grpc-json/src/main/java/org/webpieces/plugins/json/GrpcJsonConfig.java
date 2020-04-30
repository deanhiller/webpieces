package org.webpieces.plugins.json;

import java.util.ArrayList;
import java.util.List;

public class GrpcJsonConfig {

	private Class<? extends GrpcJsonCatchAllFilter> filterClazz;
	private String baseUrl;
	private List<ServiceMetaInfo> services = new ArrayList<>();

	public GrpcJsonConfig(String baseUrl, Class<? extends GrpcJsonCatchAllFilter> filterClazz, String controller, String grpcService) {
		this.baseUrl = baseUrl;
		this.filterClazz = filterClazz;
		addService(controller, grpcService);
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public Class<? extends GrpcJsonCatchAllFilter> getFilterClazz() {
		return filterClazz;
	}

	public void addService(String controller, String grpcService) {
		if(!grpcService.endsWith("Grpc"))
			throw new IllegalArgumentException("grpcService should end with Grpc");
		services.add(new ServiceMetaInfo(controller, grpcService));
	}

	public List<ServiceMetaInfo> getServices() {
		return services;
	}
	
}
