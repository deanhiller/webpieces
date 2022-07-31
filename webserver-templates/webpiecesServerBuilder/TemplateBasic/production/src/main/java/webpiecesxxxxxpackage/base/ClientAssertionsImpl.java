package webpiecesxxxxxpackage.base;

import org.webpieces.plugin.hibernate.Em;
import org.webpieces.util.context.ClientAssertions;

import javax.persistence.EntityManager;

public class ClientAssertionsImpl implements ClientAssertions {
    @Override
    public void throwIfCannotGoRemote() {
        EntityManager entityManager = Em.get();
        if(entityManager != null && entityManager.getTransaction().isActive())
            throw new IllegalStateException("Do not call remote apis when in a database transaction");
    }
    
}
