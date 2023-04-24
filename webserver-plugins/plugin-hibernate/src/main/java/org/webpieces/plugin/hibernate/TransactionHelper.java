package org.webpieces.plugin.hibernate;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.util.function.Supplier;

/**
 * @deprecated Replaced by {@link PersistenceHelper}.
 */
@Deprecated
@Singleton
public class TransactionHelper {

    private final EntityManagerFactory factory;
    private final TxCompleters txCompleters;

    @Inject
    public TransactionHelper(EntityManagerFactory factory, TxCompleters txCompleters) {
        this.factory = factory;
        this.txCompleters = txCompleters;
    }

    /**
     * Use PersistenceHelper instead
     */
    @Deprecated
    public <Resp> Resp runWithEm(Supplier<Resp> function) {
        if (Em.get() != null) {
            throw new IllegalStateException("Cannot open another entityManager when are already have one open");
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

    /**
     * Use PersistenceHelper instead
     */
    @Deprecated
    public <Resp> Resp runTransaction(Supplier<Resp> supplier) {
        return runTransaction("unknown", supplier);
    }

    /**
     * Use PersistenceHelper instead
     */
    @Deprecated
    public <Resp> Resp runTransaction(String transactionName, Supplier<Resp> supplier) {
        if (Em.get() == null) {
            return runWithEm(() -> runTransactionImpl(transactionName, supplier));
        } else {
            return runTransactionImpl(transactionName, supplier);
        }
    }

    /**
     * Use PersistenceHelper instead
     */
    @Deprecated
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

    /**
     * Use PersistenceHelper instead
     */
    @Deprecated
    private void monitorTransactionTime(String transactionName, long begin) {
//        String requestPath = Context.getMagic(MicroSvcHeader.REQUEST_PATH);
//        if (requestPath == null || requestPath.isBlank()) {
//            requestPath = "unknown";
//        }
//        Tags transactionTags = Tags.of(
//                DatabaseTransactionTags.EXECUTION_ID, transactionName,
//                DatabaseTransactionTags.REQUEST, requestPath
//        );

    }

}
