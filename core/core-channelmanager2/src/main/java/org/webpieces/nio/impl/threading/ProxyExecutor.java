package org.webpieces.nio.impl.threading;

import java.util.concurrent.Executor;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.impl.cm.basic.MDCUtil;
import org.webpieces.util.threading.SessionExecutor;

public class ProxyExecutor implements Executor {

	private Channel channel;
	private SessionExecutor executor;

	public ProxyExecutor(Channel channel, SessionExecutor executor) {
		this.channel = channel;
		this.executor = executor;
	}

	@Override
	public void execute(Runnable r) {
		ProxyRunnable runnable = new ProxyRunnable(r, channel);
		executor.execute(channel, runnable);
	}
	
	private static class ProxyRunnable implements Runnable {
		private Runnable runnable;
		private Channel channel;

		public ProxyRunnable(Runnable runnable, Channel channel) {
			this.runnable = runnable;
			this.channel = channel;
		}

		@Override
		public void run() {
			//VERY VERY special case because sometimes the future is complete already and
			//sometimes not.  Looking at this stack trace when completed quickly you will see we MDC.pust("socket)
			//in ThreadDataListener$DataListenerRunable and here
//          ProxyExecutor$ProxyRunnable.run() line: 40	
//			SessionExecutorImpl.execute(Object, Runnable) line: 93	
//			ProxyExecutor.execute(Runnable) line: 22	
//			CompletableFuture<T>.uniApplyNow(Object, Executor, Function<? super T,? extends V>) line: 677	
//			CompletableFuture<T>.uniApplyStage(Executor, Function<? super T,? extends V>) line: 658	
//			CompletableFuture<T>.thenApplyAsync(Function<? super T,? extends U>, Executor) line: 2104	
//			ThreadTCPChannel(ThreadChannel).write(ByteBuffer) line: 39	
//			SslTCPChannel$OurSslListener.sendEncryptedHandshakeData(ByteBuffer) line: 157	
//			AsyncSSLEngine3Impl.sendHandshakeMessageImpl(SSLEngine) line: 407	
//			AsyncSSLEngine3Impl.sendHandshakeMessage(SSLEngine) line: 346	
//			AsyncSSLEngine3Impl.doHandshakeWork() line: 145	
//			AsyncSSLEngine3Impl.doHandshakeLoop() line: 310	
//			AsyncSSLEngine3Impl.unwrapPacket() line: 221	
//			AsyncSSLEngine3Impl.doWork(boolean) line: 120	
//			AsyncSSLEngine3Impl.feedEncryptedPacket(ByteBuffer) line: 93	
//			SslTCPChannel$SocketDataListener.incomingData(Channel, ByteBuffer) line: 219	
//			ThreadDataListener$DataListeneRunanble.run() line: 54	
//			SessionExecutorImpl$RunnableWithKey.run() line: 143	
//			ThreadPoolExecutor.runWorker(ThreadPoolExecutor$Worker) line: 1128	
//			ThreadPoolExecutor$Worker.run() line: 628	
//			Thread.run() line: 834	

			Boolean isServerSide = channel.isServerSide();
			try {
				MDCUtil.setMDC(isServerSide, channel.getChannelId());

				runnable.run();
			} finally {
				MDCUtil.setMDC(isServerSide, channel.getChannelId());
			}
		}

	}

}
