package org.webpieces.util.context;

public interface ClientAssertions {

    /**
     * Stuff like this is asserted so you are not in a transaction when going remote
     *
     *         EntityManager em = Em.get();
     *
     *         if(em != null) {
     *             throw new IllegalStateException("You should never make remote calls while in a transaction");
     *         }
     */
    public void throwIfCannotGoRemote();

}
