package com.webpieces.http2engine.impl.shared;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TempTimeoutSet {

	private Set<Integer> toDiscard = new HashSet<Integer>();
	private List<TimeStream> timeAndStream = new ArrayList<>(); 
	
	public void add(int streamId) {
		//we give it 10 seconds of follow on packets
		long expireTime = 10*1000 + System.currentTimeMillis();
		
		toDiscard.add(streamId);
		//add in reverse order so we can remove in reverse order..
		timeAndStream.add(0, new TimeStream(expireTime, streamId));
	}
	
	public boolean checkDiscard(int streamId) {
		boolean isDiscard = toDiscard.contains(streamId);
		
		long now = System.currentTimeMillis();
		for(int i = timeAndStream.size()-1; i >= 0; i--) {
			TimeStream timeStream = timeAndStream.get(i);
			if(now < timeStream.expireTime) {
				//if this one is not expired, none are
				return isDiscard;
			}
			
			timeAndStream.remove(i);
		}
		
		return isDiscard;
	}
	
	private class TimeStream {

		public long expireTime;
		public int streamId;

		
		public TimeStream(long expireTime, int streamId) {
			this.expireTime = expireTime;
			this.streamId = streamId;
		}

	}

}
