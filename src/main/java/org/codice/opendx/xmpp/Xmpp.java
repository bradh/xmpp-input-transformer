package org.codice.opendx.xmpp;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.codice.opendx.xmpp.IXmppClient;
import ddf.catalog.CatalogFramework;

public class Xmpp implements IXmpp {

	static final Logger log = Logger.getLogger(Xmpp.class);
	private XMPPConnection connection;
	private ConnectionConfiguration config;
	private CatalogFramework catalog;
	private String login;
	private String password;
	private String nickname;
	private String server;
	private String port;
	private String room;
	private String sASLAuthenticationEnabled;
	
	public void setCatalog(CatalogFramework catalog) {
		this.catalog = catalog;
	}
	private IXmppClient service;

	 public void init() throws IOException, XMPPException, ParseException, InterruptedException{
		    
		 	login = service.getLogin();
		 	password = service.getPassword();
		 	nickname = service.getNickname();
		 	port = service.getPort();
		 	room = service.getRoom();
		 	server = service.getServer();
		 	sASLAuthenticationEnabled = service.getsASLAuthenticationEnabled();
		 	
		    log.info("Starting XmppClient");
		    log.info("The port is "+port);
		    log.info("The login is "+login);
		  
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
