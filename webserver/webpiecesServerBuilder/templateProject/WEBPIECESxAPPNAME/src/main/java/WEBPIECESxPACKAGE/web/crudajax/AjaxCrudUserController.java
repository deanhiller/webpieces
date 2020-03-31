package WEBPIECESxPACKAGE.web.crudajax;

import static WEBPIECESxPACKAGE.web.crudajax.AjaxCrudUserRouteId.AJAX_LIST_USERS;

import java.util.List;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.Current;
import org.webpieces.plugins.hibernate.Em;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Redirect;
import org.webpieces.router.api.controller.actions.Render;

import WEBPIECESxPACKAGE.db.UserDbo;
import WEBPIECESxPACKAGE.db.UserRole;

@Singleton
public class AjaxCrudUserController {

	private static Logger log = LoggerFactory.getLogger(AjaxCrudUserController.class);
	
	public Action userList() {
		EntityManager mgr = Em.get();
		Query query = mgr.createNamedQuery("findAllUsers");
		@SuppressWarnings("unchecked")
		List<UserDbo> users = query.getResultList();
		boolean showEditPopup = Current.flash().isShowEditPopup();
		return Actions.renderThis(
				"users", users,
				"showPopup", showEditPopup);
	}
	
	public Action userAddEdit(Integer id) {		
		if(id == null) {
			return Actions.renderThis(
					"entity", new UserDbo(),
					"password", null);
		}

		UserDbo user = Em.get().find(UserDbo.class, id);
		return Actions.renderThis(
				"entity", user,
				"password", null);
	}

	public Redirect postSaveUser(UserDbo entity, String password) {
		//TODO: if we wire in JSR303 bean validation into the platform, it could be 
		//done there as well though would
		//need to figure out how to do i18n for the messages in that case
		if(password == null) {
			Current.validation().addError("password", "password is required");
		} else if(password.length() < 4) {
			Current.validation().addError("password", "Value is too short");
		}
		
		if(entity.getFirstName() == null) {
			Current.validation().addError("entity.firstName", "First name is required");
		} else if(entity.getFirstName().length() < 3) {
			Current.validation().addError("entity.firstName", "First name must be more than 2 characters");
		}

		//all errors are grouped and now if there are errors redirect AND fill in
		//the form with what the user typed in along with errors
		if(Current.validation().hasErrors()) {
			log.info("page has errors");
			Current.flash().setError("Errors in form below");
			return Actions.redirectFlashAllSecure(AJAX_LIST_USERS, Current.getContext(), "password");
		}
		
		//In an AJAX form, we post a special _showEditPopup in the form parameters.  We need to clear this out
		//so the page does not load the popup.  Above, if there are errors, we use that flag to popup a window.
		//This also helps ajax forms and their data survive logging in(ie. user types data in and is redirected to
		//a login page.  After he logs in, he comes back to his ajax window with the data still there...yeah!!!)
		//we need to reset this so we don't show the edit popup
		Current.flash().setShowEditPopup(false);
		
		Current.flash().setMessage("User successfully saved");
		Current.flash().keep();
		
		Em.get().merge(entity);
        Em.get().flush();
        
		return Actions.redirect(AjaxCrudUserRouteId.AJAX_LIST_USERS);
	}

	public Render confirmDeleteUser(int id) {
		UserDbo user = Em.get().find(UserDbo.class, id);
		return Actions.renderThis("entity", user);
	}

	public Redirect postDeleteUser(int id) {
		UserDbo ref = Em.get().find(UserDbo.class, id);
		List<UserRole> roles = ref.getRoles();
		for(UserRole r : roles) {
			Em.get().remove(r);
		}
		
		Em.get().remove(ref);
		Em.get().flush();
		Current.flash().setMessage("User deleted");
		Current.flash().keep();
		return Actions.redirect(AjaxCrudUserRouteId.AJAX_LIST_USERS);
	}
}
