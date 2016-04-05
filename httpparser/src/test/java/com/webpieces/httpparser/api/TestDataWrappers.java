package com.webpieces.httpparser.api;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.webpieces.httpparser.impl.data.AbstractDataWrapper;


public class TestDataWrappers {

	private DataWrapperGenerator dataGen = HttpParserFactory.createDataWrapperGenerator();
	
	@Test
	public void testBasic() {
		
		DataWrapper wrapper1 = dataGen.wrapByteArray("0123456789".getBytes());
		DataWrapper wrapper2 = dataGen.wrapByteArray("9876543210".getBytes());
		
		DataWrapper chainDataWrappers = dataGen.chainDataWrappers(wrapper1, wrapper2);

		List<DataWrapper> split = dataGen.split(chainDataWrappers, 12);
		DataWrapper split1 = split.get(0);
		DataWrapper split2 = split.get(1);
		
		DataWrapper wrapper3 = dataGen.wrapByteArray("abcdefghij".getBytes());
		
		DataWrapper parent = dataGen.chainDataWrappers(split2, wrapper3);
		
		AbstractDataWrapper rightSide = (AbstractDataWrapper) dataGen.split(parent, 12).get(0);
	
		Assert.assertEquals(5, rightSide.getNumLayers());
	}

}
