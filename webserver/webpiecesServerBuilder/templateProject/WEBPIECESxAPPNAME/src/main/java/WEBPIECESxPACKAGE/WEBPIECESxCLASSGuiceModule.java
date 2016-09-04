package WEBPIECESxPACKAGE;

import com.google.inject.Binder;
import com.google.inject.Module;

public class WEBPIECESxCLASSGuiceModule implements Module {

	//This is where you would put the guice bindings you need though generally if done
	//right, you won't have much in this file.
	
	//If you need more Guice Modules as you want to scale, just modify WEBPIECESxCLASSMeta which returns
	//the list of all the Guice Modules in your application
	@Override
	public void configure(Binder binder) {
	}

}
