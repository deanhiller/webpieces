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
package org.playorm.nio.api.testutil.nioapi;

import java.nio.channels.ClosedChannelException;
import java.util.EventListener;


/**
 * FILL IN JAVADOC HERE
 *
 * @author $Author: cac_qjiang $
 * @version $ProductVersion:$ $FileVersion:$ $Revision: 1.1 $ $Date: 2007/08/03 02:43:18 $
 * @since $ProductVersionCreated:$ Jul 30, 2003
 */
public interface ChannelRegistrationListener extends EventListener {

//--------------------------------------------------------------------
//	FIELDS/MEMBERS
//--------------------------------------------------------------------

//--------------------------------------------------------------------
//	CONSTRUCTORS
//--------------------------------------------------------------------
//--------------------------------------------------------------------
//	BUSINESS METHODS
//--------------------------------------------------------------------
//--------------------------------------------------------------------
//	JAVABEANS GET/SET METHODS
//--------------------------------------------------------------------
//--------------------------------------------------------------------
//EVENT HANDLERS BELOW
//--------------------------------------------------------------------
	public void processRegistrations();
//--------------------------------------------------------------------
//Event Handler LIBRARY helpers
//These functions are like library functions that the event handlers above
//share to get their work done.
//--------------------------------------------------------------------
//--------------------------------------------------------------------
//	ADD LISTENERS/FIRE EVENT METHODS
//--------------------------------------------------------------------
//--------------------------------------------------------------------
//	INTERFACES/CLASSES
//--------------------------------------------------------------------

    public void waitForFinish(boolean waitForWakeup) throws InterruptedException, ClosedChannelException;
}