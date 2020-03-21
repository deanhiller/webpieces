package org.webpieces.ssl.api;

import java.time.Duration;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;

public class SSLMetrics {


	private DistributionSummary fromSocket;
	private DistributionSummary toSocket;
	private DistributionSummary fromClient;
	private DistributionSummary toClient;

	public SSLMetrics(MeterRegistry metrics) {
		fromSocket = DistributionSummary
			    .builder("ssl.fromsocket.size")
			    .distributionStatisticBufferLength(100)
				.distributionStatisticExpiry(Duration.ofMinutes(10))
			    .publishPercentiles(0.5, 0.99, 1)
			    .baseUnit("bytes") // optional (1)
			    .register(metrics);

		toSocket = DistributionSummary
			    .builder("ssl.tosocket.size")
			    .distributionStatisticBufferLength(100)
				.distributionStatisticExpiry(Duration.ofMinutes(10))
			    .publishPercentiles(0.5, 0.99, 1)
			    .baseUnit("bytes") // optional (1)
			    .register(metrics);
		
		fromClient = DistributionSummary
			    .builder("ssl.fromclient.size")
			    .distributionStatisticBufferLength(100)
				.distributionStatisticExpiry(Duration.ofMinutes(10))
			    .publishPercentiles(0.5, 0.99, 1)
			    .baseUnit("bytes") // optional (1)
			    .register(metrics);
		
		toClient = DistributionSummary
			    .builder("ssl.toclient.size")
			    .distributionStatisticBufferLength(100)
				.distributionStatisticExpiry(Duration.ofMinutes(10))
			    .publishPercentiles(0.5, 0.99, 1)
			    .baseUnit("bytes") // optional (1)
			    .register(metrics);
		
	}
	
	public void recordEncryptedBytesFromSocket(int remaining) {
		fromSocket.record(remaining);
	}

	public void recordEncryptedToSocket(int remaining) {
		toSocket.record(remaining);
	}

	public void recordPlainBytesToClient(int remaining) {
		toClient.record(remaining);
	}

	public void recordPlainBytesFromClient(int remaining) {
		fromClient.record(remaining);
	}

}
