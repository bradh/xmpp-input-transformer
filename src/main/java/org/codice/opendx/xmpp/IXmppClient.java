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

import ddf.catalog.CatalogFramework;



public interface IXmppClient  extends Serializable{
	
	void setLogin(String login);
	void setPassword(String password);
	void setNickname(String nickname);
	void setServer(String server);
	void setPort(String port);
	void setRoom(String room);
	void setsASLAuthenticationEnabled(String sASLAuthenticationEnabled);
	String getLogin();
	String getPassword();
	String getNickname();
	String getServer();
	String getPort();
	String getRoom();
	String getsASLAuthenticationEnabled();
	CatalogFramework getCatalog();
	void setCatalog(CatalogFramework catalog);
	String getpDirectory();
	void setpDirectory(String pDirectory);
	
}
