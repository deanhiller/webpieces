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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Singleton
public class TransactionHelper {

    private final EntityManagerFactory factory;
    private final TxCompleters txCompleters;
    private final MeterRegistry meterRegistry;

    @Inject
    public TransactionHelper(EntityManagerFactory factory, TxCompleters txCompleters, MeterRegistry meterRegistry) {
        this.factory = factory;
        this.txCompleters = txCompleters;
        this.meterRegistry = meterRegistry;
    }

    public <Resp> Resp runWithEm(Supplier<Resp> function) {
        if (Em.get() != null) {
            throw new IllegalStateException("Cannot open another entity manager when one is already open");
        }

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

    public <T> T run(Function<EntityManager,T> function) {

        EntityManager em = Em.get();

        if(em == null) {
            em = factory.createEntityManager();
            Em.set(em);
        }

        try {
            T rval = function.apply(em);
            em.close();
            return rval;
        } catch (Throwable t) {
            txCompleters.closeEm(t, em);
            throw t;
        } finally {
            Em.set(null);
        }

    }

    /**
     * @deprecated Use {@link #runTransaction(String, Supplier)} instead where you can supply a name for the transaction
     */
    @Deprecated
    public <Resp> Resp runTransaction(Supplier<Resp> supplier) {
        return runTransaction("unknown", supplier);
    }

    public <Resp> Resp runTransaction(String transactionName, Supplier<Resp> supplier) {
        if (Em.get() == null) {
            return runWithEm(() -> runTransactionImpl(transactionName, supplier));
        } else {
            return runTransactionImpl(transactionName, supplier);
        }
    }

    private <Resp> Resp runTransactionImpl(String transactionName, Supplier<Resp> supplier) {
        long begin = System.currentTimeMillis();

        EntityTransaction tx = Em.get().getTransaction();
        if (tx.isActive()) {
            throw new IllegalStateException("Cannot open another transaction when one is already open");
        }

        tx.begin();

        try {
            Resp resp = supplier.get();
            tx.commit();
            return resp;
        } catch (RuntimeException e) {
            txCompleters.rollbackTx(e, tx);
            // Rethrow with suppressed exception if the rollback fails too
            throw e;
        } finally {
            monitorTransactionTime(transactionName, begin);
        }
    }

    private void monitorTransactionTime(String transactionName, long begin) {
        String requestPath = "unknown";
        if (Current.isContextSet() && Current.request().relativePath != null && !Current.request().relativePath.isBlank()) {
            requestPath = Current.request().relativePath;
        }
        Tags transactionTags = Tags.of(
                DatabaseTransactionTags.TRANSACTION, transactionName,
                DatabaseTransactionTags.REQUEST, requestPath
        );

        meterRegistry.timer(DatabaseMetric.TRANSACTION_TIME.getDottedMetricName(), transactionTags)
                .record(System.currentTimeMillis() - begin, TimeUnit.MILLISECONDS);
    }

}
