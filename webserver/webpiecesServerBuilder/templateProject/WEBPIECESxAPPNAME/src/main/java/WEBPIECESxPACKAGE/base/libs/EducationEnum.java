package WEBPIECESxPACKAGE.base.libs;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.webpieces.router.api.ObjectStringConverter;

public enum EducationEnum {

	KINDERGARTEN('k', "Kindergarten"), 
	ELEMENTARY('e', "Elementary School"), 
	MIDDLE_SCHOOL('m', "Middle School"), 
	HIGH_SCHOOL('h', "High School"), 
	COLLEGE('c', "College");

	private final static Map<Character, EducationEnum> enums = new HashMap<>();
	
	static {
		for(EducationEnum level : EducationEnum.values()) {
			enums.put(level.getDbCode(), level);
		}
	}
	
	//could use an int....
	private Character dbCode;
	private String guiLabel;
	
	private EducationEnum(Character dbCode, String guiLabel) {
		this.dbCode = dbCode;
		this.guiLabel = guiLabel;
	}

	public Character getDbCode() {
		return dbCode;
	}

	public void setDbCode(Character dbCode) {
		this.dbCode = dbCode;
	}
	
	public static EducationEnum lookup(Character code) {
		return enums.get(code);
	}
	
	public String getGuiLabel() {
		return guiLabel;
	}

	@Converter
	public static class EducationConverter implements AttributeConverter<EducationEnum, Character>  {

	    public Character convertToDatabaseColumn( EducationEnum value ) {
	        if ( value == null ) {
	            return null;
	        }

	        return value.getDbCode();
	    }

	    public EducationEnum convertToEntityAttribute( Character value ) {
	        if ( value == null ) {
	            return null;
	        }

	        return EducationEnum.lookup( value );
	    }
	}
	
	public static class WebConverter implements ObjectStringConverter<EducationEnum> {

	    public String objectToString( EducationEnum value ) {
	        if ( value == null ) {
	            return null;
	        }

	        return value.getDbCode()+"";
	    }

	    public EducationEnum stringToObject( String value ) {
	        if ( value == null ) {
	            return null;
	        }

	        if(value.length() != 1)
	        	throw new IllegalArgumentException("cannot convert="+value);
	        Character c = value.charAt(0);
	        return EducationEnum.lookup( c );
	    }

		@Override
		public Class<EducationEnum> getConverterType() {
			return EducationEnum.class;
		}

	}
}
