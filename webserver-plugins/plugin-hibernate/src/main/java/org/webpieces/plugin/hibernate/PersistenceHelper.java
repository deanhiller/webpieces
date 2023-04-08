package org.webpieces.plugin.hibernate;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.webpieces.microsvc.api.MicroSvcHeader;
import org.webpieces.microsvc.monitoring.api.Monitoring;
import org.webpieces.plugin.hibernate.metrics.DatabaseMetric;
import org.webpieces.plugin.hibernate.metrics.DatabaseTransactionTags;
import org.webpieces.util.context.Context;

@Singleton
public class PersistenceHelper {

    private final EntityManagerFactory factory;
    private final TxCompleters txCompleters;
    private Monitoring monitoring;

    @Inject
    public PersistenceHelper(
            EntityManagerFactory factory,
            TxCompleters txCompleters,
            Monitoring monitoring) {
        this.factory = factory;
        this.txCompleters = txCompleters;
        this.monitoring = monitoring;
    }

    public void withVoidSession(String executionId, Consumer<EntityManager> consumer) {
        withSession(executionId, entityManager -> {
            consumer.accept(entityManager);
            return null;
        });
    }

    public <T> T withSession(String executionId, Function<EntityManager, T> function) {

        if (Em.get() != null) {
            throw new IllegalStateException("Cannot open another entityManager when are already have one open");
        }

        return runWithHibernateSession(executionId, function);

    }

    private <T> T runWithHibernateSession(String executionId, Function<EntityManager, T> function) {

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

    public void withVoidTx(String executionId, Consumer<EntityManager> consumer) {
        withTx(executionId, entityManager -> {
            consumer.accept(entityManager);
            return null;
        });
    }

    public <T> T withTx(String executionId, Function<EntityManager, T> function) {

        if(Em.get() == null) {
            return withSession(executionId, (Function<EntityManager,T>)(em) -> executeTransactionImpl(executionId, function));
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

        String requestPath = Context.getMagic(MicroSvcHeader.REQUEST_PATH);

        if((requestPath == null ) || requestPath.isBlank()) {
            requestPath = "unknown";
        }

        Map<String, String> dimensions = Map.of(
                DatabaseTransactionTags.EXECUTION_ID, transactionName,
                DatabaseTransactionTags.REQUEST, requestPath
        );

        monitoring.duration(DatabaseMetric.EXECUTION_TIME, dimensions, start, end);
    }

}
