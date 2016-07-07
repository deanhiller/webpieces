package org.webpieces.router.impl;

import org.webpieces.router.api.exceptions.NotFoundException;

public interface ErrorRoutes {
	MatchResult fetchNotfoundRoute(NotFoundException e);
	MatchResult fetchInternalServerErrorRoute();
}
