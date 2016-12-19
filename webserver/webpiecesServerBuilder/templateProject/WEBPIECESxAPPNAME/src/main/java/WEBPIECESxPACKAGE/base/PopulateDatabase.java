package WEBPIECESxPACKAGE.base;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.webpieces.router.api.Startable;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import WEBPIECESxPACKAGE.base.libs.UserDbo;

public class PopulateDatabase implements Startable {

	private static final Logger log = LoggerFactory.getLogger(PopulateDatabase.class);
	private EntityManagerFactory factory;

	@Inject
	public PopulateDatabase(EntityManagerFactory factory) {
		this.factory = factory;
	}
	
	@Override
	public void start() {
		createSomeData();
	}

	private void createSomeData() {
		EntityManager mgr = factory.createEntityManager();
		List<UserDbo> users = UserDbo.findAll(mgr);
		if(users.size() > 0)
			return; //This database has users, exit immediately to not screw up existing data 
		
		EntityTransaction tx = mgr.getTransaction();
		tx.begin();

		UserDbo user1 = new UserDbo();
		user1.setEmail("dean@somewhere.com");
		user1.setName("SomeName");
		user1.setFirstName("Dean");
		user1.setLastName("Hill");

		UserDbo user2 = new UserDbo();
		user2.setEmail("bob@somewhere.com");
		user2.setName("Bob'sName");
		user2.setFirstName("Bob");
		user2.setLastName("LastBob");
		
		log.info("classloader="+user1.getClass().getClassLoader());
		
		mgr.persist(user1);
		mgr.persist(user2);

		mgr.flush();
		
		tx.commit();
	}

}
