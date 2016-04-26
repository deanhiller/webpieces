package org.webpieces.netty.impl;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.webpieces.netty.api.BufferPool;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.exceptions.RuntimeInterruptedException;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.impl.util.ChannelSessionImpl;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyTCPChannel implements TCPChannel {
	private Bootstrap bootstrap = new Bootstrap();
	private io.netty.channel.Channel channel;
	private DataListener listener;
	private BufferPool pool;
	private ChannelSession session = new ChannelSessionImpl();

	public NettyTCPChannel(BufferPool pool) {
		this.pool = pool;
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		bootstrap.group(workerGroup);
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new IncomingDataHandler());
            }
        });
	}

	private class IncomingDataHandler extends ChannelInboundHandlerAdapter {
	    @Override
	    public void channelRead(ChannelHandlerContext ctx, Object msg) {
	        ByteBuf m = (ByteBuf) msg; // (1)
	        ByteBuffer[] nioBuffers = m.nioBuffers();
	        pool.recordBufferToBufMappingForRelease(nioBuffers, m);
	        for(ByteBuffer buffer : nioBuffers) {
	        	listener.incomingData(NettyTCPChannel.this, buffer);
	        }
	    }

	    @Override
	    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
	        cause.printStackTrace();
	        ctx.close();
	    }
	}
	
	@Override
	public CompletableFuture<Channel> connect(SocketAddress addr) {
		try {
			CompletableFuture<Channel> promise = new CompletableFuture<>();
			ChannelFuture f = bootstrap.connect(addr).sync();
			channel = f.channel();
			f.addListener(new MyFutureAdaptor(promise, this));
			
			return promise;
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		}
	}

	@Override
	public CompletableFuture<Channel> write(ByteBuffer b) {
		ByteBufAllocator alloc = channel.config().getAllocator();
		//couldn't find a way just to wrap ByteBuffer in ByteBuf
		final ByteBuf time = alloc.buffer(b.remaining()); 
		time.writeBytes(b);
		ChannelFuture writeFuture = channel.writeAndFlush(time);
		CompletableFuture<Channel> promise = new CompletableFuture<>();
		writeFuture.addListener(new MyFutureAdaptor(promise, this));
		return promise;
	}

	@Override
	public CompletableFuture<Channel> close() {
		ChannelFuture future = channel.close();
		CompletableFuture<Channel> promise = new CompletableFuture<>();
		future.addListener(new MyFutureAdaptor(promise, this));
		return promise;
	}

	@Override
	public void registerForReads(DataListener listener) {
		this.listener = listener;
	}

	@Override
	public void unregisterForReads() {
		this.listener = null;
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isConnected() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ChannelSession getSession() {
		return session;
	}

	@Override
	public void setWriteTimeoutMs(int timeout) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getWriteTimeoutMs() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setReuseAddress(boolean b) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setName(String string) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void bind(SocketAddress addr) {
		bootstrap.bind(addr);
	}

	@Override
	public boolean isBlocking() {
		return false;
	}

	@Override
	public boolean isClosed() {
		return !channel.isOpen();
	}

	@Override
	public boolean isBound() {
		throw new UnsupportedOperationException();
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean getKeepAlive() {
		throw new UnsupportedOperationException(
				"now way to get the value from netty? as need to get there for the default value");
	}

	@Override
	public void setKeepAlive(boolean keepAlive) {
		bootstrap.option(ChannelOption.SO_KEEPALIVE, keepAlive);
	}

	public void setChannelImpl(io.netty.channel.Channel channel2) {
		this.channel = channel2;
	}

}
