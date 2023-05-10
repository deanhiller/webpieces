package org.webpieces.router.impl.routers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.impl.model.MatchResult2;
import org.webpieces.util.SneakyThrow;

public abstract class AbstractRouterImpl implements AbstractRouter {

	protected MatchInfo matchInfo;

	public AbstractRouterImpl(MatchInfo matchInfo) {
		this.matchInfo = matchInfo;
	}
	
	@Override
	public MatchInfo getMatchInfo() {
		return matchInfo;
	}
	
	@Override
	public MatchResult2 matches(RouterRequest request, String subPath) {
		Matcher matcher = matchesAndParseParams(request, subPath);
		if(matcher == null)
			return new MatchResult2(false);
		else if(!matcher.matches())
			return new MatchResult2(false);

		Map<String, String> namesToValues = new HashMap<>();
		for(String name : matchInfo.getPathParamNames()) {
			String value = matcher.group(name);
			if(value == null) 
				throw new IllegalArgumentException("Bug, something went wrong. request="+request);
			//convert special characters back to their normal form like '+' to ' ' (space)
			String decodedVal = urlDecode(value);
			namesToValues.put(name, decodedVal);
		}
		
		return new MatchResult2(namesToValues);
	}
	
	protected abstract Matcher matchesAndParseParams(RouterRequest request, String subPath);

	private String urlDecode(Object value) {
		try {
			return URLDecoder.decode(value.toString(), matchInfo.getUrlEncoding().name());
		} catch(UnsupportedEncodingException e) {
			throw SneakyThrow.sneak(e);
		}
	}
}
