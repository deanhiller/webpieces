package org.webpieces.plugins.json;

public class JacksonConfig {

	private String filterPattern;
	private Class<? extends JacksonCatchAllFilter> filterClazz;
	private int filterApplyLevel;

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

	public int getFilterApplyLevel() {
		return filterApplyLevel;
	}

	public void setFilterApplyLevel(int filterApplyLevel) {
		this.filterApplyLevel = filterApplyLevel;
	}
	
}
