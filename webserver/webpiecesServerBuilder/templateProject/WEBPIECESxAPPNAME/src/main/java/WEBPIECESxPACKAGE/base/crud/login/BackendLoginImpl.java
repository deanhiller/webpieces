package WEBPIECESxPACKAGE.base.crud.login;

import org.webpieces.plugins.backend.login.BackendLogin;

public class BackendLoginImpl implements BackendLogin {

	@Override
	public boolean isLoginValid(String username, String password) {
		if(username.equals("admin") && password.equals("admin"))
			return true;
		return false;
	}

}
