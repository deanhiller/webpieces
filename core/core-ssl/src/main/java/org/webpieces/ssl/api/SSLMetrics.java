package org.webpieces.ssl.api;

import java.time.Duration;

import org.webpieces.util.acking.AckMetrics;
import org.webpieces.util.metrics.MetricsCreator;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;

public class SSLMetrics {


	private DistributionSummary fromSocket;
	private DistributionSummary toSocket;
	private DistributionSummary fromClient;
	private DistributionSummary toClient;
	private AckMetrics decryptionAckMetrics;
	private AckMetrics encryptionAckMetrics;

	public SSLMetrics(String name, MeterRegistry metrics) {
		//tags are MUCH cheaper(0) in some clouds than adding a new metric so metric names 
		//AND instead use tags to separate it out
		fromSocket = MetricsCreator.createSizeDistribution(metrics, name, "ssl", "fromsocket");
		toSocket = MetricsCreator.createSizeDistribution(metrics, name, "ssl", "tosocket");
		fromClient = MetricsCreator.createSizeDistribution(metrics, name, "ssl", "fromclient");
		toClient = MetricsCreator.createSizeDistribution(metrics, name, "ssl", "toClient");

		decryptionAckMetrics = new AckMetrics(metrics, name+".ssl.decryption");
		encryptionAckMetrics = new AckMetrics(metrics, name+".ssl.encryption");
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

	public AckMetrics getEncryptionAckMetrics() {
		return encryptionAckMetrics;
	}

	public AckMetrics getDecryptionAckMetrics() {
		return decryptionAckMetrics;
	}

}
