package org.webpieces.router.impl.body;

import java.nio.charset.Charset;

import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.data.api.DataWrapper;

public interface BodyParser {

	void parse(DataWrapper body, RouterRequest routerRequest, Charset encoding);

}
