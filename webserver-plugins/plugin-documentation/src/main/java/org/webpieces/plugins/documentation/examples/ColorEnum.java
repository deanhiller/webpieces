package org.webpieces.plugins.documentation.examples;

import java.util.HashMap;
import java.util.Map;

import org.webpieces.router.api.extensions.ObjectStringConverter;


public enum ColorEnum {

	WHITE("White", "white"), BLUE("Blue", "blue"), RED("Red", "red"), GREEN("Green", "green");
	
	private String label;
	private String databaseCode;

	private static Map<String, ColorEnum> lookupColor = new HashMap<>();
	
	static {
		for(ColorEnum color : ColorEnum.values()) {
			lookupColor.put(color.databaseCode, color);
		}
	}
	
	ColorEnum(String label, String databaseCode) {
		this.label = label;
		this.databaseCode = databaseCode;
	}

	public String getLabel() {
		return label;
	}

	public String getDatabaseCode() {
		return databaseCode;
	}
	
	public static ColorEnum lookupByCode(String code) {
		return lookupColor.get(code);
	}
	
	public static class WebConverter implements ObjectStringConverter<ColorEnum> {

	    public String objectToString( ColorEnum value ) {
	        if ( value == null ) {
	            return null;
	        }

	        return value.getDatabaseCode();
	    }

	    public ColorEnum stringToObject( String value ) {
	        if ( value == null ) {
	            return null;
	        }

	        return ColorEnum.lookupByCode( value );
	    }

		@Override
		public Class<ColorEnum> getConverterType() {
			return ColorEnum.class;
		}
	}
}