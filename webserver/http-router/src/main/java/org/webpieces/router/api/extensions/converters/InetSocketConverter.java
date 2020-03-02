package org.webpieces.router.api.extensions.converters;

import java.net.InetSocketAddress;

import org.webpieces.router.api.extensions.ObjectStringConverter;
import org.webpieces.util.cmdline2.InetConverter;

public class InetSocketConverter implements ObjectStringConverter<InetSocketAddress> {

	private InetConverter inetConverter = new InetConverter();
	
	@Override
	public Class<InetSocketAddress> getConverterType() {
		return InetSocketAddress.class;
	}

	@Override
	public InetSocketAddress stringToObject(String value) {
		return inetConverter.convertInet(value);
	}

	@Override
	public String objectToString(InetSocketAddress value) {
		return inetConverter.convertBack(value);
	}

}
