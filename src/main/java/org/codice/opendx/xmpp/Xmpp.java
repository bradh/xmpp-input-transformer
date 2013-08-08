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
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import ddf.catalog.CatalogFramework;

import java.util.ArrayList;
import java.util.List;

public class Xmpp implements IXmpp {

	static final Logger log = Logger.getLogger(Xmpp.class);
	
	private CatalogFramework catalog;
	private String pDirectory;
	
	
	private List<XMPPConnection> connections = new ArrayList<XMPPConnection>();
	
	public void setCatalog(CatalogFramework catalog) {
		this.catalog = catalog;
	}
	

	public void init() throws IOException, XMPPException, ParseException, InterruptedException, InvalidSyntaxException{
		    
		 	
		  }

	 public void destroy() throws IOException {
		    log.info("Stopping XmppClient");
		    for(XMPPConnection connection : connections){
		    connection.disconnect();
		  }
	 }
	 
	 
	 
	 public void bind(ServiceReference service) throws XMPPException, InterruptedException{
		 
		 	String login = (String)service.getProperty("login");
		 	String password = (String)service.getProperty("password");
		 	String nickname = (String)service.getProperty("nickname");
		 	String port = (String)service.getProperty("port");
		 	String room = (String)service.getProperty("room");
		 	String server = (String)service.getProperty("server");
		 	String sASLAuthenticationEnabled = (String)service.getProperty("sASLAuthenticationEnabled").toString();
		 	
		    
		    int packetReplyTimeout = 500;
			SmackConfiguration.setPacketReplyTimeout(packetReplyTimeout);
	        
			int portNum = Integer.parseInt(port);
			Boolean sasl = Boolean.getBoolean(sASLAuthenticationEnabled);
			ConnectionConfiguration config = new ConnectionConfiguration(server, portNum);
	        config.setSASLAuthenticationEnabled(sasl);
	        if (sasl){
	        	config.setSecurityMode(SecurityMode.enabled);
	        }else{
	        	config.setSecurityMode(SecurityMode.disabled);
	        }
	        
	        XMPPConnection connection = new XMPPConnection(config);
	        if(!connections.contains(connection)){
	        
	        	
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
	        listener.setCatalog(this.catalog);
	        listener.setpDirectory(this.pDirectory);
	        log.info(this.pDirectory+" is a directory");
	        muc.addMessageListener(listener);
	        
	        
	        }
	        connections.add(connection);
		 	}
	 }
	 
	 public void unbind(ServiceReference service){
		 String login = (String)service.getProperty("login");
		 
		for(XMPPConnection connection : connections){
			
			if(connection.getUser()==login){
				connection.disconnect();
			}
			
		}
	        
	        
		 
		 
	 }


	public String getpDirectory() {
		return pDirectory;
	}


	public void setpDirectory(String pDirectory) {
		this.pDirectory = pDirectory;
	}
	 
	
}
