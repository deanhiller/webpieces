package com.webpieces.http2engine.impl.shared;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.util.threading.SessionExecutor;

import com.webpieces.http2engine.api.ConnectionReset;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class Level2Synchro {

	protected Level4AbstractStreamMgr level3;
	protected SessionExecutor executor;
	protected Level3ParsingAndRemoteSettings parsing;

	public Level2Synchro(Level4AbstractStreamMgr level3, Level3ParsingAndRemoteSettings parsing, SessionExecutor executor) {
		this.level3 = level3;
		this.parsing = parsing;
		this.executor = executor;
	}

	public CompletableFuture<Void> sendMoreStreamData(Stream stream, PartialStream data) {
		return executor.executeCall(this, () -> { 
			return level3.sendMoreStreamData(stream, data);
		});
	}
	
	public void parse(DataWrapper newData) {
		//important, this forces the engine to a virtual single thread(each engine/socket has one virtual thread)
		//this makes it very easy not to have bugs AND very easy to test AND for better throughput, you can
		//just connect more sockets
		executor.execute(this, () -> { 
			parsing.parse(newData);
		});
		
	}
	
	public void farEndClosed() {
		//important, this forces the engine to a virtual single thread(each engine/socket has one virtual thread)
		//this makes it very easy not to have bugs AND very easy to test AND for better throughput, you can
		//just connect more sockets
		executor.execute(this, () -> { 
			ConnectionReset reset = new ConnectionReset("Far end closed the socket", true);
			level3.sendClientResetsAndSvrGoAway(reset );
		});		
	}
	
	public void initiateClose(String reason) {
		//important, this forces the engine to a virtual single thread(each engine/socket has one virtual thread)
		//this makes it very easy not to have bugs AND very easy to test AND for better throughput, you can
		//just connect more sockets
		executor.execute(this, () -> { 
			
		});		
	}
}
