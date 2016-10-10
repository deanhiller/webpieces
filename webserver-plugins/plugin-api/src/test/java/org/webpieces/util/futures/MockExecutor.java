package org.webpieces.util.futures;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class MockExecutor implements Executor {

	public List<Runnable> runnables = new ArrayList<>();
	
	@Override
	public void execute(Runnable command) {
		this.runnables.add(command);
	}

	public void runRunnables() {
		List<Runnable> copy = new ArrayList<>();
		copy.addAll(runnables);
		runnables.clear();
		
		for(Runnable r : copy) {
			r.run();
		}
	}

}
