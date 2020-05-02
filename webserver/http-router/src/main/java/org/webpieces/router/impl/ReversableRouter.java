package org.webpieces.router.impl;

import org.webpieces.router.impl.routers.MatchInfo;

public interface ReversableRouter {

	String getFullPath();

	MatchInfo getMatchInfo();

}
