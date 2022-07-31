package webpiecesxxxxxpackage.deleteme.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.webpieces.router.api.extensions.SimpleStorage;

import webpiecesxxxxxpackage.db.SimpleStorageDbo;

public class SimpleStorageImpl implements SimpleStorage {
	
	private EntityManagerFactory factory;

	@Inject
	public SimpleStorageImpl(EntityManagerFactory factory) {
		this.factory = factory;
	}

	@Override
	public XFuture<Void> save(String key, String subKey, String value) {
		EntityManager mgr = factory.createEntityManager();
		mgr.getTransaction().begin();

		SimpleStorageDbo bean = SimpleStorageDbo.find(mgr, key, subKey);
		if(bean == null)
			bean = new SimpleStorageDbo(key, subKey, value);
		mgr.merge(bean);
		
		mgr.flush();
		mgr.getTransaction().commit();
		mgr.close();
		
		return XFuture.completedFuture(null);
	}
	
	@Override
	public XFuture<Void> save(String key, Map<String, String> properties) {
		EntityManager mgr = factory.createEntityManager();
		mgr.getTransaction().begin();

		for(Map.Entry<String, String> entry : properties.entrySet()) {
			//should fix this to query once instead of once for each property on the bean
			SimpleStorageDbo bean = SimpleStorageDbo.find(mgr, key, entry.getKey());
			if(bean == null) {
				bean = new SimpleStorageDbo(key, entry.getKey(), entry.getValue());
			} else {
				bean.setValue(entry.getValue());
			}
			
			mgr.merge(bean);
		}
		
		mgr.flush();
		mgr.getTransaction().commit();
		mgr.close();
		
		return XFuture.completedFuture(null);
	}

	@Override
	public XFuture<Map<String, String>> read(String key) {
		EntityManager mgr = factory.createEntityManager();
		//mgr.getTransaction().begin();
		
		List<SimpleStorageDbo> rows = SimpleStorageDbo.findAll(mgr, key);
		Map<String, String> properties = new HashMap<>();
		for(SimpleStorageDbo row: rows) {
			properties.put(row.getMapKey(), row.getValue());
		}
		
		mgr.close();
		
		return XFuture.completedFuture(properties);
	}

	@Override
	public XFuture<Void> delete(String key) {
		EntityManager mgr = factory.createEntityManager();
		mgr.getTransaction().begin();

		List<SimpleStorageDbo> rows = SimpleStorageDbo.findAll(mgr, key);
		for(SimpleStorageDbo row: rows) {
			mgr.remove(row);
		}
		
		mgr.flush();
		mgr.getTransaction().commit();
		mgr.close();
		
		return XFuture.completedFuture(null);
	}

	@Override
	public XFuture<Void> delete(String key, String subKey) {
		EntityManager mgr = factory.createEntityManager();
		mgr.getTransaction().begin();

		SimpleStorageDbo row = SimpleStorageDbo.find(mgr, key, subKey);
		if(row != null)
			mgr.remove(row);
		
		mgr.flush();
		mgr.getTransaction().commit();
		mgr.close();
		
		return XFuture.completedFuture(null);
	}
}
