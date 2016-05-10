package org.webpieces.ssl.api;

import java.nio.ByteBuffer;
import java.util.List;

public class Action {

	private ActionState actionState;
	private List<ByteBuffer> toSendToSocket;
	private Runnable runnableToRun;
	
	public Action(List<ByteBuffer> toSendToSocket) {
		this(ActionState.SEND_TO_SOCKET, toSendToSocket);
	}
	
	public Action(Runnable runnableToRun) {
		this.actionState = ActionState.RUN_RUNNABLE;
		this.runnableToRun = runnableToRun;
	}
	
	public Action(ActionState actionState) {
		this.actionState = actionState;
	}

	public Action(ActionState state, List<ByteBuffer> buffersToSend) {
		this.actionState = state;
		this.toSendToSocket = buffersToSend;
	}

	public ActionState getActionState() {
		return actionState;
	}
	
	public List<ByteBuffer> getToSendToSocket() {
		if(actionState != ActionState.SEND_TO_SOCKET && actionState != ActionState.CONNECTED_AND_SEND_TO_SOCKET)
			throw new IllegalStateException("This method can only be called if action is SEND_TO_SOCKET or CONNECTED_AND_SEND_TO_SOCKET");
		return toSendToSocket;
	}
	public Runnable getRunnableToRun() {
		if(actionState != ActionState.RUN_RUNNABLE)
			throw new IllegalStateException("This method can only be called if action is RUN_RUNNABLE");
		return runnableToRun;
	}
	
	
}
