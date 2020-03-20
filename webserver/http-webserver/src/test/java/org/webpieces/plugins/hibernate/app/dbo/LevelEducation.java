package org.webpieces.plugins.hibernate.app.dbo;

import org.webpieces.router.api.extensions.ObjectStringConverter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.HashMap;
import java.util.Map;

public enum LevelEducation {

	//NOT_CHOSEN('n', ""), 
	KINDERGARTEN('k', "Kindergarten"), 
	ELEMENTARY('e', "Elementary School"), 
	MIDDLE_SCHOOL('m', "Middle School"), 
	HIGH_SCHOOL('h', "High School"), 
	COLLEGE('c', "College");

	private final static Map<Character, LevelEducation> enums = new HashMap<>();
	
	static {
		for(LevelEducation level : LevelEducation.values()) {
			enums.put(level.getDbCode(), level);
		}
	}
	
	//could use an int....
	private Character dbCode;
	private String guiLabel;
	
	private LevelEducation(Character dbCode, String guiLabel) {
		this.dbCode = dbCode;
		this.guiLabel = guiLabel;
	}

	public Character getDbCode() {
		return dbCode;
	}

	public void setDbCode(Character dbCode) {
		this.dbCode = dbCode;
	}
	
	public static LevelEducation lookup(Character code) {
		return enums.get(code);
	}
	
	public String getGuiLabel() {
		return guiLabel;
	}

	@Converter
	public static class DatabaseConverter implements AttributeConverter<LevelEducation, Character> {

	    public Character convertToDatabaseColumn( LevelEducation value ) {
	        if ( value == null ) {
	            return null;
	        }

	        return value.getDbCode();
	    }

	    public LevelEducation convertToEntityAttribute( Character value ) {
	        if ( value == null ) {
	            return null;
	        }

	        return LevelEducation.lookup( value );
	    }
	}
	
	public static class WebConverter implements ObjectStringConverter<LevelEducation> {

		@Override
		public String objectToString(LevelEducation value) {
	        if ( value == null ) {
	            return null;
	        }

	        return value.getDbCode()+"";
	    }

		public LevelEducation stringToObject(String value) {
	        if ( value == null ) {
	            return null;
	        }

	        if(value.length() != 1)
	        	throw new IllegalArgumentException("cannot convert="+value);
	        Character c = value.charAt(0);
	        return LevelEducation.lookup( c );
	    }

		
		public Class<LevelEducation> getConverterType() {
			return LevelEducation.class;
		}

	}
}
