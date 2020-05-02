package org.webpieces.plugins.hibernate;

public class HibernateConfiguration {

	private String filterRegExPath;

	public HibernateConfiguration(String filterRegExPath) {
		super();
		this.filterRegExPath = filterRegExPath;
	}

	public HibernateConfiguration() {
		super();
	}

	public String getFilterRegExPath() {
		return filterRegExPath;
	}

	public void setFilterRegExPath(String filterRegExPath) {
		this.filterRegExPath = filterRegExPath;
	}

}
