package webpiecesxxxxxpackage.deleteme.service;

import org.webpieces.plugin.hibernate.PersistenceHelper;
import org.webpieces.router.api.extensions.SimpleStorage;
import org.webpieces.util.futures.XFuture;
import webpiecesxxxxxpackage.db.SimpleStorageDbo;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleStorageImpl implements SimpleStorage {
	
	private PersistenceHelper persistenceHelper;

	@Inject
	public SimpleStorageImpl(
			//Provider so hibernate is not created on startup so serverless can start when
			//database is down and webpages not needing database can be served
			PersistenceHelper persistenceHelper
	) {
		this.persistenceHelper = persistenceHelper;
	}

	@Override
	public XFuture<Void> save(String key, String subKey, String value) {
		persistenceHelper.withVoidTx("saveB", (em)-> saveImpl(em, key, subKey, value));
		return XFuture.completedFuture(null);
	}

	private void saveImpl(EntityManager mgr, String key, String subKey, String value) {
		SimpleStorageDbo bean = SimpleStorageDbo.find(mgr, key, subKey);
		if(bean == null)
			bean = new SimpleStorageDbo(key, subKey, value);
		mgr.merge(bean);

		mgr.flush();
	}

	@Override
	public XFuture<Void> save(String key, Map<String, String> properties) {
		persistenceHelper.withVoidTx("saveB", (em)-> saveImpl(em, key, properties));
		return XFuture.completedFuture(null);
	}

	private void saveImpl(EntityManager mgr, String key, Map<String, String> properties) {
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
	}

	@Override
	public XFuture<Map<String, String>> read(String key) {
		Map<String, String> properties = persistenceHelper.withSession("readKey", (em) -> readImpl(em, key));
		return XFuture.completedFuture(properties);
	}

	private Map<String, String> readImpl(EntityManager mgr, String key) {
		List<SimpleStorageDbo> rows = SimpleStorageDbo.findAll(mgr, key);
		Map<String, String> properties = new HashMap<>();
		for(SimpleStorageDbo row: rows) {
			properties.put(row.getMapKey(), row.getValue());
		}
		return properties;
	}

	@Override
	public XFuture<Void> delete(String key) {
		persistenceHelper.withVoidTx("deleteB", (em)-> deleteImpl(em, key));
		return XFuture.completedFuture(null);
	}

	private void deleteImpl(EntityManager mgr, String key) {
		List<SimpleStorageDbo> rows = SimpleStorageDbo.findAll(mgr, key);
		for(SimpleStorageDbo row: rows) {
			mgr.remove(row);
		}

		mgr.flush();
	}

	@Override
	public XFuture<Void> delete(String key, String subKey) {
		persistenceHelper.withVoidTx("deleteC", (em)-> deleteImpl(em, key, subKey));
		return XFuture.completedFuture(null);
	}

	private void deleteImpl(EntityManager mgr, String key, String subKey) {
		SimpleStorageDbo row = SimpleStorageDbo.find(mgr, key, subKey);
		if(row != null)
			mgr.remove(row);

		mgr.flush();
	}
}
