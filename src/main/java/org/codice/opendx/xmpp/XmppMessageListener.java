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



import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import com.berico.clavin.GeoParser;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Metacard;
import ddf.catalog.operation.CreateRequestImpl;
import ddf.catalog.source.IngestException;
import ddf.catalog.source.SourceUnavailableException;


public class XmppMessageListener implements PacketListener {
	 	public GeoParser geoParser;
	 	public GeoParser getGeoParser() {
			return geoParser;
		}

		public void setGeoParser(GeoParser geoParser) {
			this.geoParser = geoParser;
		}

		public CatalogFramework getCatalog() {
			return catalog;
		}

		public void setCatalog(CatalogFramework catalog) {
			this.catalog = catalog;
		}

		public CatalogFramework catalog; 

        public void processPacket(Packet packet) {
            Message message = (Message) packet;
            if(message.getBody()!=null && message.getBody()!="" && message.getFrom()!=null && message.getFrom()!=""){
            	XmppInputTransformer xit = new XmppInputTransformer(catalog);
            	xit.setGeoParser(geoParser);
            	try {
					Metacard metacard = xit.transform(message);
					if(metacard!=null){
						insertMetacard(metacard);
					}
				} catch (Exception e) {
					
					e.printStackTrace();
				}
            }

            
		}

        private void insertMetacard(Metacard metacard) throws SourceUnavailableException, IngestException {
            CreateRequestImpl create = new CreateRequestImpl(metacard);
            catalog.create(create);
          }

}
