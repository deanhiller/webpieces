package org.webpieces.frontend2.impl;

import org.webpieces.data.api.DataWrapper;

public class InitiationResult {

	private DataWrapper leftOverData;
	private InitiationStatus initStatus;

	public InitiationResult(DataWrapper leftOverData, InitiationStatus initStatus) {
		this.leftOverData = leftOverData;
		this.initStatus = initStatus;
	}

	public InitiationResult(InitiationStatus initStatus) {
		this.initStatus = initStatus;
	}

	public InitiationStatus getInitialStatus() {
		return initStatus;
	}

	public DataWrapper getLeftOverData() {
		return leftOverData;
	}


}
