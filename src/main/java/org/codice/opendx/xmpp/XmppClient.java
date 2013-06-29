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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;

import ddf.catalog.CatalogFramework;

public class XmppClient implements IXmppClient {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8441276665745228719L;
	
	private String login;
	private String password;
	private String nickname;
	private String server;
	private String port;
	private String room;
	private String sASLAuthenticationEnabled;
	private XMPPConnection connection;
	private ConnectionConfiguration config;
	
	private CatalogFramework catalog;
	  public CatalogFramework getCatalog() {
		return catalog;
	}


	public void setCatalog(CatalogFramework catalog) {
		this.catalog = catalog;
	}
	public void setsASLAuthenticationEnabled(String sASLAuthenticationEnabled) {
		this.sASLAuthenticationEnabled = sASLAuthenticationEnabled;
	}
	
	public String getLogin() {
		return login;
	}
	public String getPassword() {
		return password;
	}
	public String getNickname() {
		return nickname;
	}
	public String getServer() {
		return server;
	}
	public String getPort() {
		return port;
	}
	public String getRoom() {
		return room;
	}
	public String getsASLAuthenticationEnabled() {
		return sASLAuthenticationEnabled;
	}
	
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
	public void setPort(String port) {
		this.port = port;
	}
	public void setRoom(String room) {
		this.room = room;
	}
	
	
	static final Logger log = Logger.getLogger(XmppClient.class);
	
	
	 public void init() throws IOException, XMPPException, ParseException, InterruptedException{
		    log.info("Starting XmppClient");
		    log.info("The port is "+port);
		  //  xcm = new XmppChatMgr(server, port, room, login, password, sASLAuthenticationEnabled, nickname);
		    
		   // XmppMessageListener listener = new XmppMessageListener();
		   // xcm.join(nickname,3600);
		   // xcm.addMessageListener(listener);
		    int packetReplyTimeout = 500;
			SmackConfiguration.setPacketReplyTimeout(packetReplyTimeout);
	        
			int portNum = Integer.parseInt(port);
			Boolean sasl = Boolean.parseBoolean(sASLAuthenticationEnabled);
	        config = new ConnectionConfiguration(server, portNum);
	        config.setSASLAuthenticationEnabled(sasl);
	        if (sasl){
	        	config.setSecurityMode(SecurityMode.enabled);
	        }else{
	        	config.setSecurityMode(SecurityMode.disabled);
	        }
	        
	        connection = new XMPPConnection(config);
	        log.info("Starting Connection");
	        connection.connect();
	        Thread.sleep(3600);
	        if(connection.isConnected()){
	        connection.login(login, password);
	        
	        }
	        Thread.sleep(3600);
	        if(connection.isAuthenticated()){
	        MultiUserChat muc = new MultiUserChat(connection, room);
	        Thread.sleep(3600);
	        while(!muc.isJoined()){
	        	try{
	        		DiscussionHistory hist = new DiscussionHistory();
	        		hist.setMaxStanzas(5);
	        	muc.join(nickname, password, hist, 360000);
	        	} catch (XMPPException x){
	        		x.printStackTrace();
	        	}
	        	try {
					Thread.sleep(3600);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	        XmppMessageListener listener = new XmppMessageListener();
	        listener.setCatalog(catalog);
	        muc.addMessageListener(listener);
		    
	        }
		    
		    
		  }

	 public void destroy() throws IOException {
		    log.info("Stopping XmppClient");
		    connection.disconnect();
		  }
	 
}


