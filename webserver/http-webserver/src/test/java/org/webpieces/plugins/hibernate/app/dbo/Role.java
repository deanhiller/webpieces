package org.webpieces.plugins.hibernate.app.dbo;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.webpieces.router.api.extensions.ObjectStringConverter;

public enum Role {
	BADASS('b', "Badass"), 
	JERK('j', "Jerk"), 
	DELINQUINT('d', "Delinquint"), 
	FOOL('f', "Fool"),
	MANAGER('m', "Manager"),
	FATHER('a', "Father"),
	MOTHER('o', "Mother"),
	;

	private final static Map<Character, Role> enums = new HashMap<>();
	
	static {
		for(Role level : Role.values()) {
			enums.put(level.getDbCode(), level);
		}
	}

	//could use an int....
	private Character dbCode;
	private String guiLabel;

	private Role(Character dbCode, String guiLabel) {
		this.dbCode = dbCode;
		this.guiLabel = guiLabel;
	}

	public Character getDbCode() {
		return dbCode;
	}

	public void setDbCode(Character dbCode) {
		this.dbCode = dbCode;
	}
	
	public static Role lookup(Character code) {
		return enums.get(code);
	}
	
	public String getGuiLabel() {
		return guiLabel;
	}

	@Converter
	public static class RoleEnumConverter implements AttributeConverter<Role, Character>  {

	    public Character convertToDatabaseColumn( Role value ) {
	        if ( value == null ) {
	            return null;
	        }

	        return value.getDbCode();
	    }

	    public Role convertToEntityAttribute( Character value ) {
	        if ( value == null ) {
	            return null;
	        }

	        return Role.lookup( value );
	    }
	}
	
	public static class WebConverter implements ObjectStringConverter<Role> {

	    public String objectToString( Role value ) {
	        if ( value == null ) {
	            return null;
	        }

	        return value.getDbCode()+"";
	    }

	    public Role stringToObject( String value ) {
	        if ( value == null ) {
	            return null;
	        }

	        if(value.length() != 1)
	        	throw new IllegalArgumentException("cannot convert="+value);
	        Character c = value.charAt(0);
	        return Role.lookup( c );
	    }

		@Override
		public Class<Role> getConverterType() {
			return Role.class;
		}
	}
}
