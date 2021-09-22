package org.webpieces.plugin.hibernate;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.webpieces.ctx.api.Current;
import org.webpieces.plugin.hibernate.metrics.DatabaseMetric;
import org.webpieces.plugin.hibernate.metrics.DatabaseTransactionTags;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Singleton
public class TransactionHelper {

	private static final String UNKNOWN_TRANSACTION_NAME = "unknown";

	private final EntityManagerFactory factory;
	private final TxCompleters txCompleters;
	private final MeterRegistry meterRegistry;
	private final TxHelperConfig config;

	@Inject
	public TransactionHelper(EntityManagerFactory factory, TxCompleters txCompleters, MeterRegistry meterRegistry, TxHelperConfig config) {
		this.factory = factory;
		this.txCompleters = txCompleters;
		this.meterRegistry = meterRegistry;
		this.config = config;
	}

	public <Resp> Resp runWithEm(Supplier<Resp> function) {
		EntityManager entityManager = Em.get();
		if (entityManager != null)
			throw new IllegalStateException("Cannot open another entityManager when are already have one open");

		EntityManager mgr = factory.createEntityManager();
		Em.set(mgr);
		try {
			Resp resp = function.get();
			mgr.close();
			return resp;
		} catch (Throwable t) {
			txCompleters.closeEm(t, mgr);
			throw t;
		} finally {
			Em.set(null);
		}
	}

	public <Resp> Resp runTransaction(String txName, final Supplier<Resp> function) {
		EntityManager em = Em.get();
		if (em == null) {
			return runWithEm(() -> runTransactionImpl(txName, function));
		} else {
			return runTransactionImpl(txName, function);
		}
	}

	// If you make any changes here, make sure to reflect those changes in the method below that takes a Function<>
	private <Resp> Resp runTransactionImpl(String transactionName, Supplier<Resp> supplier) {
		long begin = System.currentTimeMillis();

		EntityManager mgr = Em.get();
		EntityTransaction tx = mgr.getTransaction();
		if (tx.isActive()) {
			throw new IllegalStateException("You cannot call runTransaction in a transaction");
		}
		tx.begin();

		try {
			Resp resp = supplier.get();
			tx.commit();
			return resp;
		} catch(RuntimeException e) {
			txCompleters.rollbackTx(e, tx);
			throw e; //rethrow
		} finally {
			monitorTransactionTime(transactionName, begin);
		}
	}

	private void monitorTransactionTime(String transactionName, long begin) {
		Tags transactionTags = Tags.of(
				DatabaseTransactionTags.SERVICE, config.getServiceName(),
				DatabaseTransactionTags.TRANSACTION, transactionName
		);

		meterRegistry.timer(DatabaseMetric.TRANSACTION_TIME.getDottedMetricName(), transactionTags)
				.record(System.currentTimeMillis() - begin, TimeUnit.MILLISECONDS);
	}

}
