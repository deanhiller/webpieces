package org.webpieces.util.futures;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

//TODO(dhiller): Use this http2engine(client/server), http1 client, http2to11client AND frontend as
//all of them do this pattern and we should wire that all up
public class LoopingChain<T> {

	public CompletableFuture<Void> runLoop(List<T> newData, Session session, Processor<T> processFunction) {
		
		//All the below futures must be chained with previous ones in case previous ones are not
		//done which will serialize it all to be in sequence
		CompletableFuture<Void> future = session.getProcessFuture();
		
		for(T data : newData) {
			//VERY IMPORTANT: Writing the code like this would slam through calling process N times
			//BUT it doesn't give the clients a chance to seet a flag between packets
			//Mainly done for exceptions and streaming so you can log exc, set a boolean so you
			//don't get 100 exceptions while something is happening like socket disconnect
			//In these 2 lines of code, processCorrectly is CALLED N times RIGHT NOW
			//The code below this only calls them right now IF AND ONLY IF the client returns
			//a completed future each time!!!
			
			//This seems to have memory issues as well....
			//CompletableFuture<Void> temp = processFunction.process(data);
			//future = future.thenCompose(f -> temp);
			
			//future = future.thenComposeAsync( voidd -> processFunction.process(data), executor );
			future = future.thenCompose( voidd -> processFunction.process(data) );
		}
		
		//comment this out and memory leak goes away of course.......
		session.setProcessFuturee(future);
		
		return future;
	}

}
