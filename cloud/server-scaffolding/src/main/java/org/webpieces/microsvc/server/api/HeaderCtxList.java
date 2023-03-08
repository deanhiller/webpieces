package org.webpieces.microsvc.server.api;

import org.webpieces.util.context.PlatformHeaders;

import java.util.List;

public interface HeaderCtxList {

    List<PlatformHeaders> listHeaderCtxPairs();

    }
