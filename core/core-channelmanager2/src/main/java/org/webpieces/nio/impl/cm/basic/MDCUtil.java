package org.webpieces.nio.impl.cm.basic;

import org.slf4j.MDC;

public class MDCUtil {

	public static void setMDC(Boolean isServerSide, String value) {
		if(isServerSide == null)
			return;

		if(isServerSide) {
			MDC.put("svrSocket", value);
		} else {
			MDC.put("clntSocket", value);
		}			
	}
}
