package org.webpieces.plugins.hibernate;

public class HibernateConfiguration {

	private String filterRegExPath;
	private int filterApplyLevel;

	public HibernateConfiguration(String filterRegExPath) {
		this(filterRegExPath, 10000);
	}
	
	public HibernateConfiguration(String filterRegExPath, int filterApplyLevel) {
		super();
		this.filterRegExPath = filterRegExPath;
		this.setFilterApplyLevel(filterApplyLevel);
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

	public int getFilterApplyLevel() {
		return filterApplyLevel;
	}

	public void setFilterApplyLevel(int filterApplyLevel) {
		this.filterApplyLevel = filterApplyLevel;
	}

}
