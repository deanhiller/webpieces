package org.webpieces.util.cmdline2;

import java.net.InetSocketAddress;

public class InetConverter {

	public InetSocketAddress convertInet(String value) {
		if(value == null)
			return null;
		else if("".equals(value)) //if command line passes "http.port=", the value will be "" to turn off the port
			return null;
		
		int index = value.indexOf(":");
		if(index < 0)
			throw new IllegalArgumentException("Invalid format.  Format must be '{host}:{port}' or ':port' or '' (empty string for null)");
		String host = value.substring(0, index);
		String portStr = value.substring(index+1);
		try {
			int port = Integer.parseInt(portStr);
			
			if("".equals(host.trim()))
				return new InetSocketAddress(port);
			
			return new InetSocketAddress(host, port);
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("Invalid format.  The port piece of '{host}:{port}' or ':port' must be an integer");
		}
	}
	
	public String convertBack(InetSocketAddress addr) {
		if(addr == null)
			return null;
		
		return addr.getHostName()+":"+addr.getPort();
	}
}
