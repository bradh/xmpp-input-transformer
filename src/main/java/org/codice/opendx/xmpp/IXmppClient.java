/**
 * Copyright (c) Codice Foundation
 *
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 *
 **/

package org.codice.opendx.xmpp;

import java.io.Serializable;
import aQute.bnd.annotation.metatype.Meta;



public interface IXmppClient  extends Serializable{
	
	@Meta.AD(required = true, deflt = "test2")
	 String login();
	@Meta.AD(required = true, deflt = "$123qwe!")
	 String password();
	@Meta.AD(required = true, deflt = "TestUser2")
	 String nickname();
	@Meta.AD(required = true, deflt = "localhost.localdomain")
	 String server();
	@Meta.AD(required = true, deflt = "5222")
	 String port();
	@Meta.AD(required = true, deflt = "Test@conference.localhost.localdomain")
	 String room();
	@Meta.AD(required = true, deflt = "false")
	 String sASLAuthenticationEnabled();
	 
	 
	 
}
