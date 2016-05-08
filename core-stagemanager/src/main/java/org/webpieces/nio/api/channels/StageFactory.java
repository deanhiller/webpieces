package org.webpieces.nio.api.channels;

public interface StageFactory {

	//Stage createStageFactory(FromSocket from, ToSocket to);
	
	FromSocket createFromSocket(FromSocket fromSocket);
	ToSocket createToSocket(ToSocket to);
}
