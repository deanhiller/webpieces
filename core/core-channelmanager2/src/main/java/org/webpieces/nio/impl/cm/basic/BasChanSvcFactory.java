package org.webpieces.nio.impl.cm.basic;

import java.util.concurrent.Executor;

import org.webpieces.data.api.BufferPool;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.nio.api.jdk.JdkSelect;
import org.webpieces.nio.impl.ssl.SslChannelService;
import org.webpieces.nio.impl.threading.ThreadedChannelService;

import io.micrometer.core.instrument.MeterRegistry;



/**
 * @author Dean Hiller
 */
public class BasChanSvcFactory extends ChannelManagerFactory {
	
	private JdkSelect select;
	private MeterRegistry metrics;

	public BasChanSvcFactory(JdkSelect apis, MeterRegistry metrics) {
		this.select = apis;
		this.metrics = metrics;
	}

	@Override
	public ChannelManager createSingleThreadedChanMgr(String name, BufferPool pool, BackpressureConfig config) {
		BasChannelService mgr = new BasChannelService(name, select, pool, config, metrics);
		return new SslChannelService(mgr, pool, metrics);
	}

	@Override
	public ChannelManager createMultiThreadedChanMgr(String name, BufferPool pool, BackpressureConfig config, Executor executor) {
		ChannelManager mgr = createSingleThreadedChanMgr(name, pool, config);
		ThreadedChannelService mgr2 = new ThreadedChannelService(mgr, executor);
		return new SslChannelService(mgr2, pool, metrics);
	}
	
}
