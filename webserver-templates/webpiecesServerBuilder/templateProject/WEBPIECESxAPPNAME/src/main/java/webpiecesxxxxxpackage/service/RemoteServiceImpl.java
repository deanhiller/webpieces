package webpiecesxxxxxpackage.service;

import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RemoteServiceImpl implements RemoteService {

	private ExecutorService pool = Executors.newFixedThreadPool(1);
	
	@Override
	public XFuture<Integer> fetchRemoteValue(String s, int i)  {
		//Here a remote service usually returns an uncompleted future and completes it when the remote
		//service returns it's value unblocking this thread for others to use.
		//we simulate that with a threadpool that is not needed in real situations
	
		//create not yet resolved future..
		XFuture<Integer> future = new XFuture<Integer>();
		
		pool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					future.completeExceptionally(e);
					return;
				}
				
				//sleep 1 second to make sure other thread has returned back to platform
				//resolve futuere now
				future.complete(33);
				
			}
		});
		
		return future;
	}

	@Override
	public void sendData(int num) {
	}

}
