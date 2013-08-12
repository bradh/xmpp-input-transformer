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
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import com.berico.clavin.GeoParser;
import com.berico.clavin.GeoParserFactory;


import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Metacard;
import ddf.catalog.operation.CreateRequestImpl;
import ddf.catalog.source.IngestException;
import ddf.catalog.source.SourceUnavailableException;


public class XmppMessageListener implements PacketListener {
	 	
	 	private CatalogFramework catalog;
		private String pDirectory;

	 	public String getpDirectory() {
			return pDirectory;
		}

		public void setpDirectory(String pDirectory) {
			this.pDirectory = pDirectory;
		}

		public CatalogFramework getCatalog() {
			return catalog;
		}

		public void setCatalog(CatalogFramework catalog) {
			this.catalog = catalog;
		}

		static final Logger logger = Logger.getLogger(XmppMessageListener.class);
		

        public void processPacket(Packet packet) {
        	
            Message message = (Message) packet;
            if(message.getBody()!=null && message.getBody()!="" && message.getFrom()!=null && message.getFrom()!=""){
            	XmppInputTransformer xit = new XmppInputTransformer();
            	
            	
            	GeoParser geoParser = null;
            	try {
            		
            		geoParser = GeoParserFactory.getDefault(this.pDirectory);
            		
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					logger.error(e1);
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					logger.error(e1);
				}
            	xit.setCatalog(catalog);
            	xit.setGeoParser(geoParser);
            	
            	try {
					List<Metacard> metacards = xit.transform(message);
					if(metacards!=null && !metacards.isEmpty()){
						for(Metacard metacard : metacards){
						insertMetacard(metacard);
					}
					}
				} catch (Exception e) {
					
					logger.error(e);
				}
            	
            }

            
		}

        private void insertMetacard(Metacard metacard) throws SourceUnavailableException, IngestException {
            CreateRequestImpl create = new CreateRequestImpl(metacard);
            catalog.create(create);
          }

}
