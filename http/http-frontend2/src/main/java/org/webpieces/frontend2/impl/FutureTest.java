package org.webpieces.frontend2.impl;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

	public class FutureTest {
		
		private Executor exec = Executors.newFixedThreadPool(10);
		
		public static void main(String[] args) {
			new FutureTest().start();
		}
		
		
		private void start() {
			CompletableFuture<Integer> f = startProcess(5);
			
			f.cancel(false);
		}
	
	
		private CompletableFuture<Integer> startProcess(int i) {
			CompletableFuture<Integer> future1 = remoteCall(5);
			CompletableFuture<Integer> future2 = future1.thenCompose(s -> remoteCall(10));
			CompletableFuture<Integer> future3 = future2.thenCompose(s -> remoteCall(15));
			CompletableFuture<Integer> future4 = future3.thenCompose(s -> remoteCall(20));
			return future4;
		}
	
	
		public CompletableFuture<Integer> remoteCall(int value) {
			CompletableFuture<Integer> future = new CompletableFuture<Integer>();
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
