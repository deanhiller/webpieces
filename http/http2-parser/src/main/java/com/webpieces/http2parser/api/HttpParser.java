package com.webpieces.http2parser.api;

import org.webpieces.data.api.DataWrapper;

public interface HttpParser {
	ParserResult parse(DataWrapper oldData, DataWrapper moreData);
}
