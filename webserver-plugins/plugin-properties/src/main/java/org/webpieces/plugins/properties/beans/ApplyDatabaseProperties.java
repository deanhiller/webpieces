package org.webpieces.plugins.properties.beans;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.extensions.SimpleStorage;

class ApplyDatabaseProperties implements Runnable {
	
	private static final Logger log = LoggerFactory.getLogger(BeanMetaData.class);

	private SimpleStorage storage;
	private Map<String, Map<String, BeanMeta>> meta;
	private PropertyInvoker propertyInvoker;

	public ApplyDatabaseProperties(Map<String, Map<String, BeanMeta>> meta, SimpleStorage storage, PropertyInvoker propertyInvoker) {
		this.meta = meta;
		this.storage = storage;
		this.propertyInvoker = propertyInvoker;
	}

	@Override
	public void run() {
		try {
			log.debug("Starting to apply properties from database to beans.  read Database first");

			CompletableFuture<Map<String, String>> dbRead = storage.read(KeyUtil.PLUGIN_PROPERTIES_KEY);

			Map<String, String> dbProps = dbRead.get();

			for(Map.Entry<String, Map<String, BeanMeta>> entry : meta.entrySet()) {
				String category = entry.getKey();
				processBean(dbProps, category, entry.getValue());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void processBean(Map<String, String> dbProps, String category, Map<String, BeanMeta> beanMap) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		for(Map.Entry<String, BeanMeta> entry : beanMap.entrySet()) {
			String beanName = entry.getKey();
			BeanMeta beanMeta = entry.getValue();
			applyChangesToBean(dbProps, category, beanName, beanMeta);
		}
	}

	private void applyChangesToBean(Map<String, String> dbProps, String category, String beanName, BeanMeta beanMeta) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		List<PropertyInfo> props = beanMeta.getProperties();
		for(PropertyInfo prop : props) {
			String key = KeyUtil.formKey(category, beanName, prop.getName());
			String datbasePropValue = dbProps.get(key);
			if(datbasePropValue == null) {
				//as of yet, no one has saved to database, so skip it
				continue;
			}
			
			String existingPropValue = propertyInvoker.readPropertyAsString(prop);
			if(!datbasePropValue.equals(existingPropValue)) {
				try {
					propertyInvoker.writeProperty(prop, datbasePropValue);
					log.info("Property="+key+" changed from="+existingPropValue+" to new="+datbasePropValue+" and we updated the bean");
				} catch(Exception e) {
					log.info("Property="+key+" failed to update on bean="+prop.getInjectee().getClass(), e);
				}
			}
			
		}
	}
}