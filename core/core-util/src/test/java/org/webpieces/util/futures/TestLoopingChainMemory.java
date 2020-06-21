package org.webpieces.util.futures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class TestLoopingChainMemory {

	private LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
	private Executor exec = new ThreadPoolExecutor(50,50,
            0L, TimeUnit.MILLISECONDS,
            queue,
            new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					return new Thread(r);
				}
			});
	
	@Test
	public void testMemory() throws InterruptedException {
	    Runtime rt = Runtime.getRuntime();

		LoopingChain<Integer> chain = new LoopingChain<Integer>();
		
		Session s = new MySession();
		
		MyProcessor p = new MyProcessor();
		
		List<Integer> list = new ArrayList<>();
		for(int i = 0; i < 5; i++) {
			list.add(0);
		}

		long prevUsed = 0;
		for(int i = 0; i < 100_000_000; i++) {
			long used = getUsedMemoryMB(rt);
	        if (used != prevUsed) {
	        	System.out.println("used="+used+"MB  i="+i);
	            prevUsed = used;
	        }			
			
	        if(used > 500) {
	        	s.setProcessFuturee(null);
	        	rt.gc();
	        	System.out.println("queue size="+queue.size());
	        	
	        } else {
	        	if(s.getProcessFuture() == null) {
	        		//reinit to run again to use memory
	        		s.setProcessFuturee(CompletableFuture.completedFuture(null));
	        	}
	        		
	        		
	        	chain.runLoop(list, s, p);
	        }
		}
	}

	private long getUsedMemoryMB(Runtime rt) {
		long total = rt.totalMemory() / 1024 / 1024;
		long free = rt.freeMemory() / 1024 / 1024;
		long used = total - free;
		return used;
	}
	
	private class MySession implements Session {

		private CompletableFuture<Void> future = CompletableFuture.completedFuture(null);

		@Override
		public void setProcessFuturee(CompletableFuture<Void> future) {
			this.future = future;
		}

		@Override
		public CompletableFuture<Void> getProcessFuture() {
			return future;
		}
	}
	
	private class MyProcessor implements Processor<Integer> {

		@Override
		public CompletableFuture<Void> process(Integer item) {
			CompletableFuture<Void> future = new CompletableFuture<Void>();
			
			exec.execute(new Runnable() {
				@Override
				public void run() {
					future.complete(null); //complete the future
				}
			});
			
			return future;
		}
		
	}
	
    private static Map<Integer, NewObject> map = new HashMap<Integer, NewObject>();

	@Test
	public void basicMemoryTest() {
	    Runtime rt = Runtime.getRuntime();
	    long prevUsed = 0;

	    for (int j = 0; j < 4_000_000; j++) {
		    for (int i = 0; i < 8_000_000; i++) {
		        long used = getUsedMemoryMB(rt);
		        if (used != prevUsed) {
		        	System.out.println("used="+used+"MB");
		            prevUsed = used;
		        }
		        
		        
		        if(used > 500) {
		        	map.clear();
		        	rt.gc();
		        } else {
		        	map.put(i, new NewObject());
		        }
		        
		    }
	    }
	}
}
