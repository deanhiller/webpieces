package com.webpieces.data.api;

import com.webpieces.data.impl.DataWrapperGeneratorImpl;

public class DataWrapperGeneratorFactory {

	public static DataWrapperGenerator createDataWrapperGenerator() {
		return new DataWrapperGeneratorImpl();
	}
	
}
