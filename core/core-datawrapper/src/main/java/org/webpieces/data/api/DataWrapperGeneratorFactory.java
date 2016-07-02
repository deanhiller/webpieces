package org.webpieces.data.api;

import org.webpieces.data.impl.DataWrapperGeneratorImpl;

public class DataWrapperGeneratorFactory {

	public static final DataWrapper EMPTY = createDataWrapperGenerator().emptyWrapper();

	public static DataWrapperGenerator createDataWrapperGenerator() {
		return new DataWrapperGeneratorImpl();
	}
	
}
