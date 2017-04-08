package org.webpieces.webserver.https;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.webserver.filters.app.Remote;

public class MockRemote implements Remote {

	private List<Integer> recorded = new ArrayList<>();
	@Override
	public void record(Integer initialConfig) {
		recorded.add(initialConfig);
	}
	
	public List<Integer> getRecorded() {
		return recorded;
	}
}
