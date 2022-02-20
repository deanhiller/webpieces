package org.webpieces.plugin.secure.properties.beans;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.webpieces.plugin.secure.properties.PropertiesConfig;
import org.webpieces.http.exception.NotFoundException;
import org.webpieces.router.api.extensions.SimpleStorage;
import org.webpieces.router.api.extensions.Startable;
import org.webpieces.router.impl.mgmt.CachedBean;
import org.webpieces.router.impl.mgmt.ManagedBeanMeta;
import org.webpieces.router.impl.params.ObjectTranslator;
import org.webpieces.util.exceptions.SneakyThrow;

/**
 * A proxy to get around the weird circular dependency order so the guice created controller can listen
 * to all beans that are created with XXXXManaged interface(or however the user configures it)
 */
@Singleton
public class BeanMetaData implements Startable {
	
	//This is loaded during Guice construction and used AFTER all Guice is constructed within
	//a Guice context
	private List<CachedBean> cachedBeans = new ArrayList<>();
	//Because we are initially outside guice, this business logic bean is given a Provider to invoke
	//ONLY after full Guice construction is complete
	private Provider<ObjectTranslator> objectTranslatorProvider;
	private Provider<SimpleStorage> simpleStorageProvider;
	private Provider<ManagedBeanMeta> webpiecesBeanMetaProvider;
	private Provider<ScheduledExecutorService> schedulerProvider;
	
	private Map<String, Map<String, BeanMeta>> meta = new HashMap<>();
	private List<SingleCategory> categories;
	private PropertiesConfig config;

	public BeanMetaData(
			PropertiesConfig config, 
			Provider<ObjectTranslator> objectTranslator, 
			Provider<SimpleStorage> simpleStorageProvider,
			Provider<ManagedBeanMeta> webpiecesBeanMeta, 
			Provider<ScheduledExecutorService> schedulerProvider
	) {
		this.config = config;
		this.objectTranslatorProvider = objectTranslator;
		this.simpleStorageProvider = simpleStorageProvider;
		this.webpiecesBeanMetaProvider = webpiecesBeanMeta;
		this.schedulerProvider = schedulerProvider;
	}

	//NOTE: This is executed while GUICE is setting up so we are not in GUICE at this point
	public void afterInjection(Object injectee, Class<?> interfaze) {
		cachedBeans.add(new CachedBean(injectee, interfaze));
	}
	
	@Override
	public void start() {
		//We are NOW inside Guice and can call the providers
		ManagedBeanMeta webpiecesBeans = webpiecesBeanMetaProvider.get();
		ObjectTranslator objectTranslator = objectTranslatorProvider.get();
		SimpleStorage storage = simpleStorageProvider.get();

		//let's add any webpieces platform beans(we use our own stuff here)
		cachedBeans.addAll(webpiecesBeans.getBeans());
		
		for(CachedBean bean : cachedBeans) {
			loadBean(objectTranslator, bean.getInjectee(), bean.getInterfaze());
		}
		
		loadFromDbAndSetProperties(storage, new PropertyInvoker(objectTranslator));
	}
	
	private void loadFromDbAndSetProperties(SimpleStorage storage, PropertyInvoker propertyInvoker) {
		ApplyDatabaseProperties runnable = new ApplyDatabaseProperties(meta, storage, propertyInvoker);
		//On startup, kick off the Runnable that re-applies any changes in the database
		runnable.run();  //we don't catch exceptions as we want to stop startup if we can't apply all settings to match the cluster
		
		//Now, re-read every so often but if we fail to read, we just keep running
		schedulerProvider.get().scheduleWithFixedDelay(runnable, 3, 1, TimeUnit.MINUTES);
	}

	public void loadBean(ObjectTranslator objectTranslator, Object injectee, Class<?> interfaze) {
		String category = "No Category Defined";
		try {
			Method method = interfaze.getMethod(config.getCategoryMethod());
			if(method.getReturnType() != String.class)
				throw new RuntimeException("Method "+config.getCategoryMethod()+" must return String for bean="+injectee.getClass().getName());
			category = (String) method.invoke(injectee);
		} catch (NoSuchMethodException e) {
			//continue on..
		} catch (IllegalAccessException | InvocationTargetException | SecurityException e) {
			throw SneakyThrow.sneak(e);
		}

		Map<String, BeanMeta> list = meta.getOrDefault(category, new HashMap<>());

		String finalKey = injectee.toString();

		Method[] methods = interfaze.getMethods();
		List<PropertyInfo> properties = create(objectTranslator, injectee, interfaze, methods);
		
		BeanMeta beanMeta = new BeanMeta(finalKey, interfaze, properties);

		list.put(finalKey, beanMeta);
		meta.put(category, list);
	}

	private List<PropertyInfo> create(ObjectTranslator objectTranslator, Object injectee, Class<?> interfaze, Method[] methods) {
		List<PropertyInfo> props = new ArrayList<>();
		for(Method m : methods) {
			//getters take 0 arguments or we don't expose them
			if(m.getParameterTypes().length == 0) {
				if(m.getName().startsWith("get") && !m.getName().equals("getCategory")) {
					props.add(create(objectTranslator, injectee, interfaze, m, 3));
				} else if(m.getName().startsWith("is")) {
					props.add(create(objectTranslator, injectee, interfaze, m, 2));
				}
			}
		}
		return props;
	}

	private PropertyInfo create(ObjectTranslator objectTranslator, Object injectee, Class<?> interfaze, Method getter, int i) {
		String name = getter.getName();
		String propertyName = name.substring(i, name.length());
		Method setter = null;
		
		try {
			//We can only do the setter methods IF there is a way to translate wired in by webpieces or their
			//application added a translater
			if(objectTranslator.getConverter(getter.getReturnType()) != null)
				setter = interfaze.getMethod("set"+propertyName, getter.getReturnType());
		} catch (NoSuchMethodException e) {
			//setter not exist
		} catch (SecurityException e) {
			throw SneakyThrow.sneak(e);
		}

		return new PropertyInfo(propertyName, injectee, getter, setter);
	}
	
	public List<SingleCategory> getCategories() {
		if(categories == null) {
			createCategoriesOnce();
		}
		
		return categories;
	}

	private void createCategoriesOnce() {
		categories = new ArrayList<>();

		for(Entry<String, Map<String, BeanMeta>> entry : meta.entrySet()) {
			Map<String, BeanMeta> beans = entry.getValue();
			List<BeanMeta> list = createList(beans);
			categories.add(new SingleCategory(entry.getKey(), list));
		}
	}

	private List<BeanMeta> createList(Map<String, BeanMeta> beans) {
		List<BeanMeta> beanMetas = new ArrayList<>();
		for(Entry<String, BeanMeta> entry : beans.entrySet()) {
			beanMetas.add(entry.getValue());
		}
		return beanMetas;
	}

	public BeanMeta getBeanMeta(String category, String name) {
		Map<String, BeanMeta> map = meta.get(category);
		if(map == null)
			throw new NotFoundException("Category="+category+" not found");
		BeanMeta beanMeta = map.get(name);
		if(beanMeta == null)
			throw new NotFoundException("Bean="+name+" not found");
		
		return beanMeta;
	}

}
