package org.codice.opendx.xmpp;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.codice.opendx.xmpp.IXmppClient;
import ddf.catalog.CatalogFramework;

public class Xmpp implements IXmpp {

	static final Logger log = Logger.getLogger(Xmpp.class);
	
	private CatalogFramework catalog;
	private ArrayList<XMPPConnection> connections = new ArrayList<XMPPConnection>();
	
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
		 log.info("Found Service "+ service);
		 	String login = (String)service.getProperty("login");
		 	String password = (String)service.getProperty("password");
		 	String nickname = (String)service.getProperty("nickname");
		 	String port = (String)service.getProperty("port");
		 	String room = (String)service.getProperty("room");
		 	String server = (String)service.getProperty("server");
		 	String sASLAuthenticationEnabled = (String)service.getProperty("sASLAuthenticationEnabled").toString();
		 	
		    log.info("Starting XmppClient");
		    log.info("The port is "+port);
		    log.info("The login is "+login);
		  
		    int packetReplyTimeout = 500;
			SmackConfiguration.setPacketReplyTimeout(packetReplyTimeout);
	        
			int portNum = Integer.parseInt(port);
			Boolean sasl = Boolean.parseBoolean(sASLAuthenticationEnabled);
			ConnectionConfiguration config = new ConnectionConfiguration(server, portNum);
	        config.setSASLAuthenticationEnabled(sasl);
	        if (sasl){
	        	config.setSecurityMode(SecurityMode.enabled);
	        }else{
	        	config.setSecurityMode(SecurityMode.disabled);
	        }
	        
	        XMPPConnection connection = new XMPPConnection(config);
	        if(!connections.contains(connection)){
	        
	        	connections.add(connection);
	        
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
	        muc.addMessageListener(listener);
		    
	        }
		 	}
	 }
	 
	 public void unbind(ServiceReference service){
		 
	 }
	 
	
}
