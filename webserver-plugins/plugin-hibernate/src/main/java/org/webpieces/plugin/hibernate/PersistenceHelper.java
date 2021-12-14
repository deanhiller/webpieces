package org.webpieces.plugin.hibernate;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

import org.webpieces.plugin.hibernate.metrics.DatabaseMetric;
import org.webpieces.plugin.hibernate.metrics.DatabaseTransactionTags;
import org.webpieces.util.context.Context;
import org.webpieces.util.context.ContextKey;

@Singleton
public class PersistenceHelper {

    private final EntityManagerFactory factory;
    private final TxCompleters txCompleters;
    private final MeterRegistry meterRegistry;

    @Inject
    public PersistenceHelper(EntityManagerFactory factory, TxCompleters txCompleters, MeterRegistry meterRegistry) {
        this.factory = factory;
        this.txCompleters = txCompleters;
        this.meterRegistry = meterRegistry;
    }

    public void execute(String executionId, Consumer<EntityManager> consumer) {
        execute(executionId, entityManager -> {
            consumer.accept(entityManager);
            return null;
        });
    }

    public <T> T execute(String executionId, Function<EntityManager, T> function) {

        if (Em.get() != null) {
            throw new IllegalStateException("Cannot open another entityManager when are already have one open");
        }

        return executeImpl(executionId, function);

    }

    private <T> T executeImpl(String executionId, Function<EntityManager, T> function) {

        EntityManager mgr = factory.createEntityManager();
        Em.set(mgr);

        long start = System.currentTimeMillis();

        try {
            T result = function.apply(mgr);
            mgr.close();
            return result;
        } catch (Throwable t) {
            txCompleters.closeEm(t, mgr);
            throw t;
        } finally {
            Em.set(null);
            long end = System.currentTimeMillis();
            monitorExecutionTime(executionId, start, end);
        }

    }

    public void executeTransaction(String executionId, Consumer<EntityManager> consumer) {
        executeTransaction(executionId, entityManager -> {
            consumer.accept(entityManager);
            return null;
        });
    }

    public <T> T executeTransaction(String executionId, Function<EntityManager, T> function) {

        if(Em.get() == null) {
            return execute(executionId, (Function<EntityManager,T>)(em) -> executeTransactionImpl(executionId, function));
        } else {
            return executeTransactionImpl(executionId, function);
        }

    }

    private <T> T executeTransactionImpl(String executionId, Function<EntityManager, T> function) {

        EntityTransaction tx = Em.get().getTransaction();
        if (tx.isActive()) {
            throw new IllegalStateException("Cannot open another transaction when one is already open");
        }

        long start = System.currentTimeMillis();

        tx.begin();

        try {
            T result = function.apply(Em.get());
            tx.commit();
            return result;
        } catch (RuntimeException e) {
            txCompleters.rollbackTx(e, tx);
            // Rethrow with suppressed exception if the rollback fails too
            throw e;
        } finally {
            long end = System.currentTimeMillis();
            monitorExecutionTime(executionId, start, end);
        }
    }

    private void monitorExecutionTime(String transactionName, long start, long end) {

        String requestPath = (String) Context.get(ContextKey.REQUEST_PATH.toString());

        if((requestPath == null ) || requestPath.isBlank()) {
            requestPath = "unknown";
        }

        Tags transactionTags = Tags.of(
            DatabaseTransactionTags.EXECUTION_ID, transactionName,
            DatabaseTransactionTags.REQUEST, requestPath
        );

        meterRegistry.timer(DatabaseMetric.EXECUTION_TIME.getDottedMetricName(), transactionTags)
                .record(end - start, TimeUnit.MILLISECONDS);

    }

}
