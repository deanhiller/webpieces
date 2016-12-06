package org.webpieces.plugins.hibernate.app;

import java.util.List;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.webpieces.ctx.api.Current;
import org.webpieces.plugins.hibernate.Em;
import org.webpieces.plugins.hibernate.app.dbo.UserDbo;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.actions.Redirect;
import org.webpieces.router.api.actions.Render;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

@Singleton
public class CrudController {
	
	private static final Logger log = LoggerFactory.getLogger(HibernateAsyncController.class);
	
	public Render userList() {
		EntityManager mgr = Em.get();
		Query query = mgr.createNamedQuery("findAllUsers");
		@SuppressWarnings("unchecked")
		List<UserDbo> users = query.getResultList();
		return Actions.renderThis("users", users);
	}
	
	public Render userAddEdit(Integer id) {
		if(id == null) {
			return Actions.renderThis();
		}
		
		EntityManager mgr = Em.get();
		UserDbo user = mgr.find(UserDbo.class, id);
		return Actions.renderThis("user", user);
	}
	
	public Redirect postSaveUser(UserDbo user) {
		if(user.getPassword().length() < 4) {
			Current.validation().addError("password", "Value is too short");
		}
		
		if(Current.validation().hasErrors()) {
			Current.flash().setMessage("Errors in form below");
			return Actions.redirectFlashAll(HibernateRouteId.ADD_EDIT_USER_PAGE, Current.getContext());
		}
		
		Current.flash().setMessage("User successfully saved");
		Em.get().merge(user);
        Em.get().flush();
        
		return Actions.redirect(HibernateRouteId.LIST_USERS);
	}
	
    public Redirect postDeleteUser(Integer id) {
    	UserDbo user = Em.get().getReference(UserDbo.class, id);
    	Em.get().remove(user);
    	Em.get().flush();
    	
    	Current.flash().setMessage("User "+user.getFirstName()+" "+user.getLastName()+" was deleted");
    	return Actions.redirect(HibernateRouteId.LIST_USERS);
    }
	
}
