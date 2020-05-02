package org.webpieces.plugins.hibernate;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class TxCompleters {

	public void commit(EntityTransaction tx, EntityManager em) {
		try {
			tx.commit();
			
			em.close();
		} catch(Throwable e) {
			closeEm(e, em);
			throw new RuntimeException(e);
		}
	}

	public void rollbackCloseSuppress(Throwable t, EntityManager mgr, EntityTransaction tx) {
		rollbackTx(t, tx);
		closeEm(t, mgr);
	}
	
	public void rollbackTx(Throwable t, EntityTransaction tx) {
		try {
			tx.rollback();
		} catch(Throwable e) {
			t.addSuppressed(e);
		}
	}
	
	public void closeEm(Throwable t, EntityManager em) {
		try {
			em.close();
		} catch(Throwable e) {
			t.addSuppressed(e);
		}
	}
}
