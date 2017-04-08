package org.webpieces.router.impl.params;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.router.api.BodyContentBinder;

public class ArgsResult {

	private List<Object> args = new ArrayList<>();
	private BodyContentBinder binder;

	public void setBinder(BodyContentBinder binder) {
		if(this.binder != null)
			throw new IllegalStateException("bug, Cannot invoke this method twice or bad things will happen.  we should be protecting from this on startup");
		this.binder = binder;
	}

	public void addArgument(Object bean) {
		args.add(bean);
	}

	public Object[] getArguments() {
		return args.toArray();
	}

	public BodyContentBinder getBinder() {
		return binder;
	}

}
