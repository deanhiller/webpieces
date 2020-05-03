package org.webpieces.plugins.hibernate;

public class HibernateConfiguration {

	private String filterRegExPath;
	private int filterApplyLevel;
	private boolean applyRegExPackage;

	@Deprecated
	public HibernateConfiguration(String filterRegExPath) {
		this(filterRegExPath, false, 500);
	}

	public HibernateConfiguration(String filterRegExPath, boolean applyRegExPackage) {
		this(filterRegExPath, true, 500);
	}
	
	public HibernateConfiguration(String filterRegExPath, boolean applyRegExPackage, int filterApplyLevel) {
		super();
		this.filterRegExPath = filterRegExPath;
		this.applyRegExPackage = applyRegExPackage;
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

	public boolean isApplyRegExPackage() {
		return applyRegExPackage;
	}

}
