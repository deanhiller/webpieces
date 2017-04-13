package WEBPIECESxPACKAGE.base.libs;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.webpieces.ctx.api.WebConverter;

public enum Education {

	KINDERGARTEN('k', "Kindergarten"), 
	ELEMENTARY('e', "Elementary School"), 
	MIDDLE_SCHOOL('m', "Middle School"), 
	HIGH_SCHOOL('h', "High School"), 
	COLLEGE('c', "College");

	private final static Map<Character, Education> enums = new HashMap<>();
	
	static {
		for(Education level : Education.values()) {
			enums.put(level.getDbCode(), level);
		}
	}
	
	//could use an int....
	private Character dbCode;
	private String guiLabel;
	
	private Education(Character dbCode, String guiLabel) {
		this.dbCode = dbCode;
		this.guiLabel = guiLabel;
	}

	public Character getDbCode() {
		return dbCode;
	}

	public void setDbCode(Character dbCode) {
		this.dbCode = dbCode;
	}
	
	public static Education lookup(Character code) {
		return enums.get(code);
	}
	
	public String getGuiLabel() {
		return guiLabel;
	}

	@Converter
	public static class EducationConverter implements AttributeConverter<Education, Character>  {

	    public Character convertToDatabaseColumn( Education value ) {
	        if ( value == null ) {
	            return null;
	        }

	        return value.getDbCode();
	    }

	    public Education convertToEntityAttribute( Character value ) {
	        if ( value == null ) {
	            return null;
	        }

	        return Education.lookup( value );
	    }
	}
	
	public static class EduConverter implements WebConverter<Education> {

	    public String objectToString( Education value ) {
	        if ( value == null ) {
	            return null;
	        }

	        return value.getDbCode()+"";
	    }

	    public Education stringToObject( String value ) {
	        if ( value == null ) {
	            return null;
	        }

	        if(value.length() != 1)
	        	throw new IllegalArgumentException("cannot convert="+value);
	        Character c = value.charAt(0);
	        return Education.lookup( c );
	    }

		@Override
		public Class<Education> getConverterType() {
			return Education.class;
		}

	}
}
