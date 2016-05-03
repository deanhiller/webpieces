package com.webpieces.data.api;

import com.webpieces.data.impl.DataWrapperGeneratorImpl;

public class DataWrapperGeneratorFactory {

	public static final DataWrapper EMPTY = createDataWrapperGenerator().emptyWrapper();

	public static DataWrapperGenerator createDataWrapperGenerator() {
		return new DataWrapperGeneratorImpl();
	}
	
}
