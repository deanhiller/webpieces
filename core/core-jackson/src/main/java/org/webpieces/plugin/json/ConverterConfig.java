package org.webpieces.plugin.json;

public class ConverterConfig {

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

	public ConverterConfig(boolean convertNullToEmptyStr) {
		this.convertNullToEmptyStr = convertNullToEmptyStr;
	}

	public boolean isConvertNullToEmptyStr() {
		return convertNullToEmptyStr;
	}

	public void setConvertNullToEmptyStr(boolean convertNullToEmptyStr) {
		this.convertNullToEmptyStr = convertNullToEmptyStr;
	}
	
	
	
}
