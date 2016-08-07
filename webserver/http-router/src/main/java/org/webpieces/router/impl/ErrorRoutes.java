package org.webpieces.router.impl;

import org.webpieces.router.api.exceptions.NotFoundException;

public interface ErrorRoutes {
	NotFoundInfo fetchNotfoundRoute(NotFoundException e);
	MatchResult fetchInternalServerErrorRoute();
}
