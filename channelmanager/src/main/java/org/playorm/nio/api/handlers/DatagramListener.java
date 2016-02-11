/*
Copyright (c) 2002, Dean Hiller
All rights reserved.

*****************************************************************
IF YOU MAKE CHANGES TO THIS CODE AND DO NOT POST THEM, YOU 
WILL BE IN VIOLATION OF THE LICENSE I HAVE GIVEN YOU.  Contact
me at deanhiller@users.sourceforge.net if you need a different
license.
*****************************************************************

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/
package org.playorm.nio.api.handlers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.playorm.nio.api.channels.DatagramChannel;


/**
 */
public interface DatagramListener {
	/**
	 * The contract here is 
	 * 1. the byteBuffer will be ready for reading from.(ie. after the 
	 * ByteBuffer is filled, flip has already been called.)
	 * 2. you MUST read all the data(ie. buffer.position() has to equal buffer.limit()
	 * 
	 * @param channel
	 * @param b
	 */
	public void incomingData(DatagramChannel channel, InetSocketAddress fromAddr, ByteBuffer b) throws IOException;

	/**
	 * This is called in the case of udp when the packet was not read by other
	 * end either because it can't get there or because the other end is not
	 * listening, etc. etc.
	 * 
	 * @param channel
	 * @param data TODO
	 * @param e
	 */
	public void failure(DatagramChannel channel, InetSocketAddress fromAddr, ByteBuffer data, Throwable e);   
}