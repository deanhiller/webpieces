package org.webpieces.webserver.impl.parsing;

import java.nio.charset.Charset;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.router.api.dto.RouterRequest;

public interface BodyParser {

	void parse(DataWrapper body, RouterRequest routerRequest, Charset encoding);

}
