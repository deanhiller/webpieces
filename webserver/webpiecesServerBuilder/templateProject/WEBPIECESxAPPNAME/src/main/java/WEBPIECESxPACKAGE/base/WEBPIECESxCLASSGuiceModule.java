package WEBPIECESxPACKAGE.base;

import org.webpieces.router.api.Startable;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;

public class WEBPIECESxCLASSGuiceModule implements Module {

	//This is where you would put the guice bindings you need though generally if done
	//right, you won't have much in this file.
	
	//If you need more Guice Modules as you want to scale, just modify WEBPIECESxCLASSMeta which returns
	//the list of all the Guice Modules in your application
	@Override
	public void configure(Binder binder) {
		//all modules have access to adding their own Startable objects to be run on server startup
		Multibinder<Startable> uriBinder = Multibinder.newSetBinder(binder, Startable.class);
	    uriBinder.addBinding().to(PopulateDatabase.class);
	}

}
