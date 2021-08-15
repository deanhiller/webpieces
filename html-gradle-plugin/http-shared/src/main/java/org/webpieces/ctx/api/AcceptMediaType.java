package org.webpieces.ctx.api;

public class AcceptMediaType {
	private boolean isMatchesAllTypes;
	private boolean isMatchesAllSubtypes;
	private String mainType;
	private String subType;
	
	public AcceptMediaType() {
		this("*");
		isMatchesAllTypes = true;
	}
	
	public AcceptMediaType(String mainType) {
		this(mainType, "*");
		isMatchesAllSubtypes = true;
	}

	public AcceptMediaType(String mainType, String subType) {
		this.mainType = mainType;
		this.subType = subType;
	}

	public boolean isMatchesAllTypes() {
		return isMatchesAllTypes;
	}

	public boolean isMatchesAllSubtypes() {
		return isMatchesAllSubtypes;
	}

	public String getMainType() {
		return mainType;
	}

	public String getSubType() {
		return subType;
	}

	@Override
	public String toString() {
		return "AcceptMediaType [isMatchesAllTypes=" + isMatchesAllTypes + ", isMatchesAllSubtypes="
				+ isMatchesAllSubtypes + ", mainType=" + mainType + ", subType=" + subType + "]";
	}

}
