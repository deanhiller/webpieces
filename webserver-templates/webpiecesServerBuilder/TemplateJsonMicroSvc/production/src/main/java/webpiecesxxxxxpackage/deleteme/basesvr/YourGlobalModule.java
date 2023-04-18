package webpiecesxxxxxpackage.deleteme.basesvr;

import com.google.inject.Binder;
import com.google.inject.Module;
import org.webpieces.util.cmdline2.Arguments;

/**
 * A global module for ALL your microservices.  Put this in re-usable place and have all ProdServerMeta's reference it
 *
 * This module is for your web app (ProdServerMeta.java)
 */
public class YourGlobalModule implements Module {

	public YourGlobalModule(Arguments cmdLineArguments) {

	}

	@Override
	public void configure(Binder binder) {
		
	}

}
