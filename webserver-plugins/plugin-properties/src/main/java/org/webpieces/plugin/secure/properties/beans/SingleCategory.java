package org.webpieces.plugin.secure.properties.beans;

import java.util.List;

public class SingleCategory {

	private String name;
	private List<BeanMeta> beanMetas;

	public SingleCategory(String category, List<BeanMeta> beanMetas) {
		this.name = category;
		this.beanMetas = beanMetas;
	}
	
	public String getName() {
		return name;
	}

	public List<BeanMeta> getBeanMetas() {
		return beanMetas;
	}
}
