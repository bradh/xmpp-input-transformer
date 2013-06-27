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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import com.berico.clavin.GeoParser;
import com.berico.clavin.resolver.ResolvedLocation;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.io.WKTWriter;
import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardImpl;
import ddf.catalog.federation.FederationException;
import ddf.catalog.operation.*;
import ddf.catalog.source.SourceUnavailableException;
import ddf.catalog.source.UnsupportedQueryException;
import ddf.catalog.transform.CatalogTransformerException;
import ddf.catalog.transform.InputTransformer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.geotools.filter.FilterFactoryImpl;
import org.jivesoftware.smack.packet.Message;
import org.opengis.filter.Filter;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;


public class XmppInputTransformer implements InputTransformer {

	private static final Logger log = Logger.getLogger(XmppInputTransformer.class);
	  private static final String CONTENT_TYPE = "application/xml";
	  public static final SimpleDateFormat ISO_8601_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	  private GeoParser geoParser;
	  public GeoParser getGeoParser() {
		return geoParser;
	}


	public void setGeoParser(GeoParser geoParser) {
		this.geoParser = geoParser;
	}


	private CatalogFramework catalog;
	  public CatalogFramework getCatalog() {
		return catalog;
	}


	public void setCatalog(CatalogFramework catalog) {
		this.catalog = catalog;
	}


	private BundleContext bundleContext;

	  public XmppInputTransformer(){
		    
		   		    
		  }
	  
	  static {
		    ISO_8601_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
		  }
	  public Metacard transform(Message message) throws Exception {
	    return transform(message, null);
	  }


	  public Metacard transform(Message message, String id) throws IOException, CatalogTransformerException, Exception {
	    log.info("Received chat message beginning transformation");

	    if (message == null) {
	      throw new CatalogTransformerException("Cannot transform null input.");
	    }

	   
	      Message newEntry = new Message();
	      if(!isEntryInCatalog(message)){
	    	  newEntry = message;
	      }
	      else{
	    	  newEntry = null;
	      }
	      

	      MetacardImpl metacard = new MetacardImpl();
	      metacard.setTitle(newEntry.getBody().replaceAll("[^a-zA-Z0-9]+", " "));

	      metacard.setContentTypeName(CONTENT_TYPE);
	      
	      try{
	        metacard.setThumbnail(IOUtils.toByteArray(new FileInputStream((this.getClass().getResource("/images/nyt.ico").getFile()))));
	      } catch (Exception e) {
	        metacard.setThumbnail(IOUtils.toByteArray((FrameworkUtil.getBundle(XmppInputTransformer.class).getResource("images/nyt.ico").openConnection().getInputStream())));
	      }

	      List<ResolvedLocation> locations = locationDataFromEntry(newEntry);

	      if(!locations.isEmpty())
	        metacard.setLocation(WKTWriter.toPoint(new Coordinate(locations.get(0).geoname.longitude, locations.get(0).geoname.latitude)));

	      metacard.setMetadata("<?xml version=\"1.0\"?>\n<metadata>\n" +
	              "<title>\n" + newEntry.getSubject().replaceAll("[^a-zA-Z0-9]+", " ") + "\n</title>\n" +
	              "<description>\n" + StringEscapeUtils.escapeXml(newEntry.getBody()) + "\n</description>\n" +
	              "<hashcode>" +getHashCode(newEntry.getBody()) + "</hashcode>\n" +
	              "\n</metadata>");
	      log.info("Metacard " + metacard.getTitle() + "(" + metacard.getId() + ")" + " created");
	      log.info("Metacard " + metacard.getMetadata() );
	      return metacard;
	    

	  }

	  private List<ResolvedLocation> getResolvedLocationsForString(String string){
	    List<ResolvedLocation> resolvedLocations = null;
	    try {
	      resolvedLocations = geoParser.parse(WordUtils.capitalize(string));
	    } catch (Exception e) {
	      log.error(e);
	    }
	    return resolvedLocations;
	  }

	  private List<ResolvedLocation> locationDataFromEntry(Message newEntry) {

	      List<ResolvedLocation> locations = getResolvedLocationsForString(newEntry.getBody());
	      
	        return locations;
	      
	      
	  }

	  private String bytesToHex(byte[] bytes) {
	    final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	    char[] hexChars = new char[bytes.length * 2];
	    int v;
	    for ( int j = 0; j < bytes.length; j++ ) {
	      v = bytes[j] & 0xFF;
	      hexChars[j * 2] = hexArray[v >>> 4];
	      hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	  }

	  private String getHashCode(String string) throws NoSuchAlgorithmException {
	    MessageDigest md = MessageDigest.getInstance("MD5");
	    md.update(string.getBytes());
	    return bytesToHex(md.digest());
	  }

	  private boolean isEntryInCatalog(Message entry) throws UnsupportedQueryException, SourceUnavailableException, FederationException, NoSuchAlgorithmException {
	    FilterFactoryImpl filterFactory = new FilterFactoryImpl() ;
	    Filter filter = filterFactory.like(filterFactory.property(Metacard.ANY_TEXT), getHashCode(entry.getBody()));
	    Query query = new QueryImpl(filter);

	    QueryRequest request = new QueryRequestImpl(query);

	    QueryResponse response = catalog.query(request);
	    log.info("found " + response.getResults().size() + " with " + entry.getBody().replaceAll("[^a-zA-Z0-9]+", " ") + " " + getHashCode(entry.getBody()));
	    return !response.getResults().isEmpty();
	  }


	public BundleContext getBundleContext() {
		return bundleContext;
	}


	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}


	public Metacard transform(InputStream arg0) throws IOException,
			CatalogTransformerException {
		// TODO Auto-generated method stub
		return null;
	}


	public Metacard transform(InputStream arg0, String arg1)
			throws IOException, CatalogTransformerException {
		// TODO Auto-generated method stub
		return null;
	}
	
}
