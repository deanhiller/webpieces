package WEBPIECESxPACKAGE.db;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.webpieces.router.api.extensions.ObjectStringConverter;

public enum RoleEnum {
	BADASS('b', "Badass"), 
	JERK('j', "Jerk"), 
	DELINQUINT('d', "Delinquint"), 
	FOOL('f', "Fool"),
	MANAGER('m', "Manager"),
	FATHER('t', "Father"),
	MOTHER('o', "Mother"),
	;

	private final static Map<Character, RoleEnum> enums = new HashMap<>();
	
	static {
		for(RoleEnum level : RoleEnum.values()) {
			enums.put(level.getDbCode(), level);
		}
	}

	//could use an int....
	private Character dbCode;
	private String guiLabel;

	private RoleEnum(Character dbCode, String guiLabel) {
		this.dbCode = dbCode;
		this.guiLabel = guiLabel;
	}

	public Character getDbCode() {
		return dbCode;
	}

	public void setDbCode(Character dbCode) {
		this.dbCode = dbCode;
	}
	
	public static RoleEnum lookup(Character code) {
		return enums.get(code);
	}
	
	public String getGuiLabel() {
		return guiLabel;
	}

	@Converter
	public static class RoleEnumConverter implements AttributeConverter<RoleEnum, Character>  {

	    public Character convertToDatabaseColumn( RoleEnum value ) {
	        if ( value == null ) {
	            return null;
	        }

	        return value.getDbCode();
	    }

	    public RoleEnum convertToEntityAttribute( Character value ) {
	        if ( value == null ) {
	            return null;
	        }

	        return RoleEnum.lookup( value );
	    }
	}
	
	public static class WebConverter implements ObjectStringConverter<RoleEnum> {

	    public String objectToString( RoleEnum value ) {
	        if ( value == null ) {
	            return null;
	        }

	        return value.getDbCode()+"";
	    }

	    public RoleEnum stringToObject( String value ) {
	        if ( value == null ) {
	            return null;
	        }

	        if(value.length() != 1)
	        	throw new IllegalArgumentException("cannot convert="+value);
	        Character c = value.charAt(0);
	        return RoleEnum.lookup( c );
	    }

		@Override
		public Class<RoleEnum> getConverterType() {
			return RoleEnum.class;
		}
	}
}
