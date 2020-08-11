package org.webpieces.plugin.json;

public class JacksonConfig {

	private String filterPattern;
	private Class<? extends JacksonCatchAllFilter> filterClazz = JacksonCatchAllFilter.class;
	private Class<? extends JacksonNotFoundFilter> notFoundFilterClazz = JacksonNotFoundFilter.class;
	private int filterApplyLevel = 1000000;
	private int notFoudFilterLevel = 1000000;
	
	//SQL makes life in java/hibernate HARD UNLESS you default all incoming data to be "" instead of null
	//Advantages
	//1. SQL constraints on "" work!!!  you cannot have John "" Smith twice in DB while you can have John null Smith twice when there is a first, middle, last name not null constraint
	//2. NamedQueries are VASTLY simplified since query.setParameter("middleName", "") works!!!!.  query.setParameter("middleName", null) guarantees 0 rows are returned EVERY time...ugh
	//3. You no longer have to check for null and can just check for empty string (one less thing to check on)
	//
	//Disadvantages
	//You basically add not null constraints to every String which means that thing you actually did require ...well, a not null constraint is not working anymore since you receive a "" instead.
	//To fix that, you can add a not empty constraint check instead.
	private boolean convertNullToEmptyStr = true;

	public JacksonConfig(String filterPattern) {
		this.filterPattern = filterPattern;
	}

	/**
	 * Use to remove the catchall filter in case you want to install your own or null it out so you don't install one here.
	 * 
	 * ONE case we ran into was installing a catchall filter for json at package name com.company.svc.json.external AND a different one
	 * at com.company.svc.json.internal 
	 * 
	 * @param filterPattern
	 * @param filterClazz
	 */
	public JacksonConfig(String filterPattern, Class<? extends JacksonCatchAllFilter> filterClazz) {
		this.filterPattern = filterPattern;
		this.filterClazz = filterClazz;
	}

	public String getFilterPattern() {
		return filterPattern;
	}

	public int getFilterApplyLevel() {
		return filterApplyLevel;
	}

	public void setFilterApplyLevel(Integer filterApplyLevel) {
		this.filterApplyLevel = filterApplyLevel;
	}

	public int getNotFoudFilterLevel() {
		return notFoudFilterLevel;
	}

	public Class<? extends JacksonCatchAllFilter> getFilterClazz() {
		return filterClazz;
	}
	
	public void setFilterClazz(Class<? extends JacksonCatchAllFilter> filterClazz) {
		this.filterClazz = filterClazz;
	}

	public Class<? extends JacksonNotFoundFilter> getNotFoundFilterClazz() {
		return notFoundFilterClazz;
	}

	public void setNotFoundFilterClazz(Class<? extends JacksonNotFoundFilter> notFoundFilterClazz) {
		this.notFoundFilterClazz = notFoundFilterClazz;
	}

	public boolean isConvertNullToEmptyStr() {
		return convertNullToEmptyStr;
	}

	public void setConvertNullToEmptyStr(boolean convertNullToEmptyStr) {
		this.convertNullToEmptyStr = convertNullToEmptyStr;
	}
	
}
