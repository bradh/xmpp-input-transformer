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


import org.apache.log4j.Logger;


public class XmppClient implements IXmppClient {
	
	
	private static final long serialVersionUID = 8441276665745228719L;
	

	static final Logger log = Logger.getLogger(XmppClient.class);
	
	private String login;
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getServer() {
		return server;
	}
	public void setServer(String server) {
		this.server = server;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getRoom() {
		return room;
	}
	public void setRoom(String room) {
		this.room = room;
	}
	public String getsASLAuthenticationEnabled() {
		return sASLAuthenticationEnabled;
	}
	public void setsASLAuthenticationEnabled(String sASLAuthenticationEnabled) {
		this.sASLAuthenticationEnabled = sASLAuthenticationEnabled;
	}
	private String password;
	private String nickname;
	private String server;
	private String port;
	private String room;
	private String sASLAuthenticationEnabled;
	
	public void init() {
	    
	 	
	  }

	public void destroy()  {
		    
	}
		
	
}
	 
	
		
		



