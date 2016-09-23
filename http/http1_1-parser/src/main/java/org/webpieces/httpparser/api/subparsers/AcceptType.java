package org.webpieces.httpparser.api.subparsers;

public class AcceptType {

	private boolean isMatchesAllTypes;
	private boolean isMatchesAllSubtypes;
	private String mainType;
	private String subType;
	
	public AcceptType() {
		this("*");
		isMatchesAllTypes = true;
	}
	
	public AcceptType(String mainType) {
		this(mainType, "*");
		isMatchesAllSubtypes = true;
	}

	public AcceptType(String mainType, String subType) {
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

}
