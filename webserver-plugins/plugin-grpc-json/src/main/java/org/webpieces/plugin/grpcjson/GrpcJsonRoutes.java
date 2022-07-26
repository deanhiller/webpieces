package org.webpieces.plugin.grpcjson;

import static org.webpieces.ctx.api.HttpMethod.POST;
import static org.webpieces.router.api.routes.Port.HTTPS;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.regex.Pattern;

import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.FilterPortType;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.util.exceptions.SneakyThrow;

import io.grpc.MethodDescriptor;
import io.grpc.ServiceDescriptor;

public class GrpcJsonRoutes implements Routes {

	private GrpcJsonConfig config;

	public GrpcJsonRoutes(GrpcJsonConfig config) {
		if(config.getBaseUrl().endsWith("/"))
			throw new IllegalArgumentException("config.getBaseUrl must not end with /");
		else if(!config.getBaseUrl().startsWith("/"))
			throw new IllegalArgumentException("config.getBaseUrl must start with /");
		this.config = config;
	}
	
	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		String baseUrl = config.getBaseUrl();
		String filterPattern = baseUrl+"/.*";
		
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();
		Pattern pattern = Pattern.compile(filterPattern);
		
		Class<GrpcJsonCatchAllFilter> filter = GrpcJsonCatchAllFilter.class;
		bldr.addFilter(filterPattern, filter, new JsonConfig(pattern, false), FilterPortType.ALL_FILTER, config.getFilterApplyLevel());		
		bldr.addNotFoundFilter(filter, new JsonConfig(pattern, true), FilterPortType.ALL_FILTER, config.getFilterApplyLevel());
		
		
		for(ServiceMetaInfo descriptor : config.getServices()) {
			loadRoutes(bldr, baseUrl, descriptor);
		}
	}

	@SuppressWarnings("rawtypes")
	private void loadRoutes(RouteBuilder bldr, String baseUrl, ServiceMetaInfo meta) {
		try {
			//MUST use Thread context class loader in case we are in DevelopmentServer.
			//This class is loaded in AppClassLoader.  We need to be using CompilingClassLoader in dev server
			ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();
			
			String controller = meta.getController().getName();
			
			//NOTE: If we do not do this, it will not recompile in the DevelopmentServer as it will use the original class
			Class grpcClazz = threadClassLoader.loadClass(meta.getGrpcService().getName());
			@SuppressWarnings("unchecked")
			Method method = grpcClazz.getMethod("getServiceDescriptor");
			ServiceDescriptor descriptor = (ServiceDescriptor) method.invoke(null);
			Collection<MethodDescriptor<?, ?>> methods = descriptor.getMethods();
	
			for (MethodDescriptor<?, ?> methodDescriptor : methods) {
				String fullMethodName = methodDescriptor.getFullMethodName();
				int index = fullMethodName.indexOf("/");
				String grpcMethod = fullMethodName.substring(index+1, fullMethodName.length());
				String javaMethodName = grpcMethod.substring(0, 1).toLowerCase() + grpcMethod.substring(1);
	
				//the world is moving ALL to HTTPS so it's not bad to just do it all on https
				//POST is fine for rpc calls.  YES, some calls may be stricly get BUT this is rpc at this point so POST
				//this plugin is EASY to copy and fork and maintain your self so you can easily do that
				bldr.addContentRoute(HTTPS, POST, baseUrl+"/" + fullMethodName, controller + "." + javaMethodName);
			}	
		} catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw SneakyThrow.sneak(e);
		}
	}

}
