package org.webpieces.plugin.hibernate;

import java.util.function.Function;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

public class TransactionHelper {

	private EntityManagerFactory factory;
	private TxCompleters txCompleters;

	@Inject
	public TransactionHelper(EntityManagerFactory factory, TxCompleters txCompleters) {
		this.factory = factory;
		this.txCompleters = txCompleters;
	}
	
	public <Req, Resp> Resp runTransaction(Function<EntityManager, Resp> function) {
		EntityManager mgr = factory.createEntityManager();
		EntityTransaction tx = mgr.getTransaction();
		tx.begin();
		
		try {
			Resp resp = function.apply(mgr);
			txCompleters.commit(tx, mgr);
			return resp;
		} catch(RuntimeException e) {
			txCompleters.rollbackCloseSuppress(e, mgr, tx);
			throw e; //rethrow
		}
	}


	
}
