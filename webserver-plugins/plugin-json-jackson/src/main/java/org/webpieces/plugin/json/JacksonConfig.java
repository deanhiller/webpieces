package org.webpieces.plugin.json;

public class JacksonConfig {

	private String filterPattern;
	private Class<? extends JacksonCatchAllFilter> filterClazz = JacksonCatchAllFilter.class;
	private Class<? extends JacksonNotFoundFilter> notFoundFilterClazz = JacksonNotFoundFilter.class;
	private int filterApplyLevel = 1000000;
	private int notFoudFilterLevel = 1000000;

	public JacksonConfig(String filterPattern) {
		this.filterPattern = filterPattern;
	}

	/**
	 * @deprecated Just use JacksonConfig(filterPattern) instead AND only set catchall filter if you are changing it
	 */
	@Deprecated
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
	
}
