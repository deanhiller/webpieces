package WEBPIECESxPACKAGE.web.login;

import org.webpieces.plugins.backend.login.BackendLogin;

public class BackendLoginImpl implements BackendLogin {

	@Override
	public boolean isLoginValid(String username, String password) {
		if("admin".equals(username) && "admin".equals(password))
			return true;
		return false;
	}

}
