package org.webpieces.plugin.json;

public class JacksonConfig {

	private String filterPattern;
	private Class<? extends JacksonCatchAllFilter> filterClazz;
	private Integer filterApplyLevel;

	public JacksonConfig(String filterPattern, Class<? extends JacksonCatchAllFilter> filterClazz) {
		this.filterPattern = filterPattern;
		this.filterClazz = filterClazz;
	}

	public String getFilterPattern() {
		return filterPattern;
	}

	public Class<? extends JacksonCatchAllFilter> getFilterClazz() {
		return filterClazz;
	}

	public Integer getFilterApplyLevel() {
		return filterApplyLevel;
	}

	public void setFilterApplyLevel(Integer filterApplyLevel) {
		this.filterApplyLevel = filterApplyLevel;
	}
	
}
