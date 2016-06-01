package org.webpieces.router.api.dto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.RouteModule;
import org.webpieces.router.api.Router;
import org.webpieces.router.api.RouterModules;
import org.webpieces.router.api.file.VirtualFile;

import com.google.inject.Module;

public class RouterConfig {
	private static final Logger log = LoggerFactory.getLogger(RouterConfig.class);
	private final RouterModules rm;
	
	public RouterConfig(VirtualFile modules) {
		try {
			rm = init(modules);
		} catch (IOException e) {
			throw new RuntimeException("Exception reading file="+modules.getName(), e);
		}
	}

	private RouterModules init(VirtualFile modules) throws IOException {
		log.info("loading the master "+RouterModules.class.getSimpleName()+" class file");		

		String moduleName;
		try (InputStream str = modules.openInputStream()) {
			InputStreamReader reader = new InputStreamReader(str);
			BufferedReader bufReader = new BufferedReader(reader);
			moduleName = bufReader.readLine().trim();
		}

		log.info(RouterModules.class.getSimpleName()+" class to load="+moduleName);
		Class<?> clazz = clazzForName(moduleName);
		Object obj = newInstance(clazz);
		if(!(obj instanceof RouterModules))
			throw new IllegalArgumentException("name="+moduleName+" does not implement "+RouterModules.class.getSimpleName());

		log.info(RouterModules.class.getSimpleName()+" loaded");
		return (RouterModules) obj;
	}

	public List<Module> getGuiceModules() {
		return rm.getGuiceModules();
	}
	
	public List<RouteModule> getRouteModules() {
		return rm.getRouterModules();
	}

	private Object newInstance(Class<?> clazz) {
		try {
			return clazz.newInstance();
		} catch (InstantiationException e) {
			throw new IllegalArgumentException("Your clazz="+clazz.getSimpleName()+" could not be created(are you missing default constructor? is it not public?)", e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Your clazz="+clazz.getSimpleName()+" could not be created", e);
		}
	}

	private Class<?> clazzForName(String moduleName) {
		try {
			return Class.forName(moduleName);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Your clazz="+moduleName+" was not found on the classpath", e);
		}
	}
}
