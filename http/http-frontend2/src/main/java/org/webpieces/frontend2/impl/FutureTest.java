package org.webpieces.frontend2.impl;

import java.util.concurrent.CancellationException;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

	public class FutureTest {
		
		private Executor exec = Executors.newFixedThreadPool(10);
		
		public static void main(String[] args) {
			new FutureTest().start();
		}
		
		
		private void start() {
			XFuture<Integer> f = startProcess(5);
			
			f.cancel(false);
		}
	
	
		private XFuture<Integer> startProcess(int i) {
			XFuture<Integer> future1 = remoteCall(5);
			XFuture<Integer> future2 = future1.thenCompose(s -> remoteCall(10));
			XFuture<Integer> future3 = future2.thenCompose(s -> remoteCall(15));
			XFuture<Integer> future4 = future3.thenCompose(s -> remoteCall(20));
			return future4;
		}
	
	
		public XFuture<Integer> remoteCall(int value) {
			XFuture<Integer> future = new XFuture<Integer>();
			exec.execute(new Runnable() {
				@Override
				public void run() {
					System.out.println("RUnning task="+value);
					
					try {
						Thread.sleep(5000);
						future.complete(value+4);
					} catch (InterruptedException e) {
						future.completeExceptionally(e);
					}
					
				}
			});
			
			return future;
		}
	
	}
