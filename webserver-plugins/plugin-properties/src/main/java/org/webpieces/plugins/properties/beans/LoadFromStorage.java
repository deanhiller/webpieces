package org.webpieces.plugins.properties.beans;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.api.SimpleStorage;
import org.webpieces.router.api.Startable;

@Singleton
public class LoadFromStorage implements Startable {

	private SimpleStorage storage;
	private BeanMetaData beanMetaData;

	@Inject
	public LoadFromStorage(SimpleStorage storage, BeanMetaData beanMetaData) {
		this.storage = storage;
		this.beanMetaData = beanMetaData;
	}

	@Override
	public void start() {
		beanMetaData.loadFromDbAndSetProperties(storage);
	}
}
