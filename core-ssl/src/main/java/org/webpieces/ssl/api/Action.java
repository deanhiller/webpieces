package org.webpieces.ssl.api;

import java.nio.ByteBuffer;
import java.util.List;

public class Action {

	private ActionState actionState;
	private List<ByteBuffer> toSend;
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
		this.toSend = buffersToSend;
	}

	public ActionState getActionState() {
		return actionState;
	}
	
	public List<ByteBuffer> getToSendToSocket() {
		if(actionState != ActionState.SEND_TO_SOCKET 
				&& actionState != ActionState.CONNECTED_AND_SEND_TO_SOCKET
				&& actionState != ActionState.CLOSED_AND_SEND_TO_SOCKET)
			throw new IllegalStateException("This method can only be called if action is "
					+ "SEND_TO_SOCKET or CONNECTED_AND_SEND_TO_SOCKET or CLOSED_AND_SEND_TO_SOCKET");
		return toSend;
	}
	public Runnable getRunnableToRun() {
		if(actionState != ActionState.RUN_RUNNABLE)
			throw new IllegalStateException("This method can only be called if action is RUN_RUNNABLE");
		return runnableToRun;
	}

	public List<ByteBuffer> getToSendToClient() {
		if(actionState != ActionState.SEND_TO_CLIENT)
			throw new IllegalStateException("This method can only be called if action is SEND_TO_CLIENT");
		return toSend;
	}
	
	
}
