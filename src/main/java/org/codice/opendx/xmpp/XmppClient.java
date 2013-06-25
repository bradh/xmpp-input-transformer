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

import java.io.IOException;
import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.jivesoftware.smack.XMPPException;

import com.berico.clavin.GeoParser;
import ddf.catalog.CatalogFramework;


public class XmppClient {
	
	private CatalogFramework catalog;
	private String login;
	private String password;
	private String nickname;
	private String server;
	private int port;
	private String room;
	private Boolean sASLAuthenticationEnabled;
	
	
	
	public void setsASLAuthenticationEnabled(Boolean sASLAuthenticationEnabled) {
		this.sASLAuthenticationEnabled = sASLAuthenticationEnabled;
	}
	private XmppChatMgr xcm;
	
	public void setLogin(String login) {
		this.login = login;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public void setServer(String server) {
		this.server = server;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public void setRoom(String room) {
		this.room = room;
	}
	
	public void setCatalog(CatalogFramework catalog) {
		this.catalog = catalog;
	}
	static final Logger log = Logger.getLogger(XmppClient.class);
	
	
	 public void init() throws IOException, XMPPException, ParseException{
		    log.info("Starting XmppClient");
		    xcm = new XmppChatMgr(server, port, room, login, password, sASLAuthenticationEnabled);
		    
		    XmppMessageListener listener = new XmppMessageListener();
		    listener.setCatalog(catalog);
		    listener.setGeoParser(new GeoParser("/opt/CLAVIN/IndexDirectory"));
		    xcm.addMessageListener(listener);
		    xcm.join(nickname,500);
		  }

	 public void destroy() throws IOException {
		    log.info("Stopping RSSInputStreamAdapter");
		    xcm.disconnect();
		  }

}
