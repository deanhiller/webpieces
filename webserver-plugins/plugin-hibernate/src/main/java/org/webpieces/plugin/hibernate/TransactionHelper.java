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
import java.util.function.Function;
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

	public <Resp> Resp runWithEm(Function<EntityManager, Resp> function) {
		EntityManager mgr = factory.createEntityManager();

		Em.set(mgr);
		try {
			Resp resp = function.apply(mgr);
			mgr.close();
			return resp;
		} catch(RuntimeException e) {
			tryClose(mgr, e);
			throw e;
		} finally {
			Em.set(null);
		}
	}

	public <Resp> Resp runWithEm(Supplier<Resp> function) {
		EntityManager mgr = factory.createEntityManager();

		Em.set(mgr);
		try {
			Resp resp = function.get();
			mgr.close();
			return resp;
		} catch(RuntimeException e) {
			tryClose(mgr, e);
			throw e;
		} finally {
			Em.set(null);
		}
	}

	// If you make any changes here, make sure to reflect those changes in the method below that takes a Function<>
	public <Resp> Resp runTransaction(String transactionName, Supplier<Resp> supplier) {
		long begin = System.currentTimeMillis();

		EntityManager mgr = factory.createEntityManager();

		EntityTransaction tx = beginTransaction(mgr);
		Em.set(mgr);

		try {
			Resp resp = supplier.get();
			txCompleters.commit(tx, mgr);
			return resp;
		} catch(RuntimeException e) {
			txCompleters.rollbackCloseSuppress(e, mgr, tx);
			throw e; //rethrow
		} finally {
			Em.set(null);

			monitorTransactionTime(transactionName, begin);
		}
	}

	// If you make any changes here, make sure to reflect those changes in the method above that takes a Supplier<>
	public <Resp> Resp runTransaction(String transactionName, Function<EntityManager, Resp> function) {
		long begin = System.currentTimeMillis();

		EntityManager mgr = factory.createEntityManager();

		EntityTransaction tx = beginTransaction(mgr);
		Em.set(mgr);

		try {
			Resp resp = function.apply(mgr);
			txCompleters.commit(tx, mgr);
			return resp;
		} catch(RuntimeException e) {
			txCompleters.rollbackCloseSuppress(e, mgr, tx);
			throw e; //rethrow
		} finally {
			Em.set(null);

			monitorTransactionTime(transactionName, begin);
		}
	}

	// This one is basically the same as the one below, but it doesn't have a finally block and it calls a slightly
	//  different method from txCompleters
	// TODO Should this one be deprecated too?
	@Deprecated
	public <Resp> Resp runTransaction(EntityManager mgr, Supplier<Resp> function) {
		EntityTransaction tx = beginTransaction(mgr);

		try {
			Resp resp = function.get();
			tx.commit();
			return resp;
		} catch(RuntimeException e) {
			txCompleters.rollbackTx(e, tx);
			throw e; //rethrow
		}
	}

	/**
	 * @deprecated Use {@link #runTransaction(String, Function)} instead
	 */
	@Deprecated
	public <Resp> Resp runTransaction(Function<EntityManager, Resp> function) {
		return runTransaction(UNKNOWN_TRANSACTION_NAME, function);
	}

	/**
	 * @deprecated Use {@link #runTransaction(String, Supplier)} instead
	 */
	@Deprecated
	public <Resp> Resp runTransaction(Supplier<Resp> function) {
		return runTransaction(UNKNOWN_TRANSACTION_NAME, function);
	}

	private EntityTransaction beginTransaction(EntityManager mgr) {
		if (mgr.getTransaction().isActive()) {
			throw new IllegalStateException("You cannot call runTransaction in a transaction");
		}

		EntityTransaction tx = mgr.getTransaction();
		tx.begin();

		return tx;
	}

	private void tryClose(EntityManager mgr, RuntimeException e) {
		try {
			mgr.close();
		} catch(RuntimeException nextExc) {
			//This exception needs to be added to original.
			//The original exception is more important
			e.addSuppressed(nextExc);
		}
	}

	private void monitorTransactionTime(String transactionName, long begin) {
		String requestPath = Current.request().originalRequest.getPath();
		if (requestPath == null || requestPath.isBlank()) {
			requestPath = "unknown";
		}

		Tags transactionTags = Tags.of(
				DatabaseTransactionTags.SERVICE, config.getServiceName(),
				DatabaseTransactionTags.REQUEST, requestPath,
				DatabaseTransactionTags.TRANSACTION, transactionName
		);

		meterRegistry.timer(DatabaseMetric.TRANSACTION_TIME.getDottedMetricName(), transactionTags)
				.record(System.currentTimeMillis() - begin, TimeUnit.MILLISECONDS);
	}

}
