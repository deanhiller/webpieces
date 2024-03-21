package org.webpieces.plugin.hibernate;

public class HibernateConfiguration {

	private String filterRegExPath = ".*";
	private int filterApplyLevel = 50;
	private boolean applyRegExPackage = true;

	private boolean transactionOnByDefault = false;

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

	public void setApplyRegExPackage(boolean applyRegExPackage) {
		this.applyRegExPackage = applyRegExPackage;
	}

	public boolean isTransactionOnByDefault() {
		return transactionOnByDefault;
	}

	public void setTransactionOnByDefault(boolean transactionOnByDefault) {
		this.transactionOnByDefault = transactionOnByDefault;
	}
}
