package com.webpieces.http2parser.api;

import org.webpieces.data.api.DataWrapper;

public interface HttpParser {
	ParserResult prepareToParse();

	ParserResult parse(ParserResult oldResult, DataWrapper moreData);
}
