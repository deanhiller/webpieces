package org.webpieces.plugin.hibernate;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.webpieces.util.exceptions.SneakyThrow;

public class TxCompleters {

	public void commit(EntityTransaction tx, EntityManager em) {
		try {
			tx.commit();
			
			em.close();
		} catch(Throwable e) {
			closeEm(e, em);
			throw SneakyThrow.sneak(e);
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
