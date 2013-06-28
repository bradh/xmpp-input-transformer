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
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.filter.*;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;



import java.util.*;


public class XmppChatMgr {

    private XMPPConnection connection;
    private String room;
    private String nickname = null;
    private boolean joined = false;
    private List<String> participants = new ArrayList<String>();
    private ConnectionConfiguration config;
    private PacketFilter presenceFilter;
    private PacketFilter messageFilter;
    private PacketCollector messageCollector;
    
 
    
    public XmppChatMgr(String server,String port,String room, String username, String password, String sASLAuthenticationEnabled) throws XMPPException {
    	
    	final Logger logger = Logger.getLogger(XmppChatMgr.class);
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
        logger.info("Starting Connection");
        connection.connect();
        connection.login(username, password);
        
        this.room = room;
        // Create a collector for all incoming messages.
        messageFilter = new AndFilter(new FromContainsFilter(room), new PacketTypeFilter(Message.class));
        messageFilter = new AndFilter(messageFilter, new PacketFilter() {
            public boolean accept(Packet packet) {
                Message msg = (Message)packet;
                return msg.getType() == Message.Type.groupchat;
            }
        });
        messageCollector = connection.createPacketCollector(messageFilter);
        // Create a listener for all presence updates.
        presenceFilter = new AndFilter(new FromContainsFilter(room),
                new PacketTypeFilter(Presence.class));
        connection.addPacketListener(new PacketListener() {
            public void processPacket(Packet packet) {
                Presence presence = (Presence)packet;
                String from = presence.getFrom();
                if (presence.getType() == Presence.Type.available) {
                    synchronized (participants) {
                        if (!participants.contains(from)) {
                            participants.add(from);
                        }
                    }
                }
                else if (presence.getType() == Presence.Type.unavailable) {
                    synchronized (participants) {
                        participants.remove(from);
                    }
                }
            }
        }, presenceFilter);
        
        
    }

    
    public String getRoom() {
        return room;
    }

    
    public synchronized void join(String nickname) throws XMPPException {
        join(nickname, SmackConfiguration.getPacketReplyTimeout());
    }

    
    public synchronized void join(String nickname, long timeout) throws XMPPException {
        if (nickname == null || nickname.equals("")) {
            throw new IllegalArgumentException("Nickname must not be null or blank.");
        }
        if (joined) {
            leave();
        }
        Presence joinPresence = new Presence(Presence.Type.available);
        joinPresence.setTo(room + "/" + nickname);
        
        PacketFilter responseFilter = new AndFilter(
                new FromContainsFilter(room + "/" + nickname),
                new PacketTypeFilter(Presence.class));
        PacketCollector response = connection.createPacketCollector(responseFilter);
        
        int i=10;
        while(i>0){
        		connection.sendPacket(joinPresence);
                
                Presence presence = (Presence)response.nextResult(timeout);
                response.cancel();
                if(i==0 && presence == null){
                	throw new XMPPException("No response from server.");
                }
                if(i==0 && presence.getError() != null){
                	 throw new XMPPException(presence.getError());
                }
                i=i-1;
        }
        
        this.nickname = nickname;
        joined = true;
    }

    
    public boolean isJoined() {
        return joined;
    }

    
    public synchronized void leave() {
       
        if (!joined) {
            return;
        }
        
        Presence leavePresence = new Presence(Presence.Type.unavailable);
        leavePresence.setTo(room + "/" + nickname);
        connection.sendPacket(leavePresence);
       
        participants = new ArrayList<String>();
        nickname = null;
        joined = false;
    }

    
    public String getNickname() {
        return nickname;
    }

    
    public int getParticipantCount() {
        synchronized (participants) {
            return participants.size();
        }
    }

    
    public Iterator<String> getParticipants() {
        synchronized (participants) {
            return Collections.unmodifiableList(new ArrayList<String>(participants)).iterator();
        }
    }

   
    public void addParticipantListener(PacketListener listener) {
        connection.addPacketListener(listener, presenceFilter);
    }

   
    public void sendMessage(String text) throws XMPPException {
        Message message = new Message(room, Message.Type.groupchat);
        message.setBody(text);
        connection.sendPacket(message);
    }

    
    public Message createMessage() {
        return new Message(room, Message.Type.groupchat);
    }

    
    public void sendMessage(Message message) throws XMPPException {
        connection.sendPacket(message);
    }

    
    public Message pollMessage() {
        return (Message)messageCollector.pollResult();
    }

    
    public Message nextMessage() {
        return (Message)messageCollector.nextResult();
    }

   
    public Message nextMessage(long timeout) {
        return (Message)messageCollector.nextResult(timeout);
    }

    public void disconnect(){
    	connection.disconnect();
    }
    
    public void addMessageListener(PacketListener listener) {
        connection.addPacketListener(listener, messageFilter);
    }

    public void finalize() throws Throwable {
        super.finalize();
        try {
            if (messageCollector != null) {
                messageCollector.cancel();
            }
        }
        catch (Exception e) {}
    }
    
        
}
