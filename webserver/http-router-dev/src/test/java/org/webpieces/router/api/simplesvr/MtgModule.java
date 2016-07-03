package org.webpieces.router.api.simplesvr;

import com.google.inject.Binder;
import com.google.inject.Module;

public class MtgModule implements Module {

	@Override
	public void configure(Binder binder) {
		binder.bind(SomeUtil.class).to(SomeUtilImpl.class);
	}

}
