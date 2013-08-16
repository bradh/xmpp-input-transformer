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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import com.berico.clavin.GeoParser;
import com.berico.clavin.GeoParserFactory;
import com.berico.clavin.extractor.LocationExtractor;
import com.berico.clavin.nerd.ExternalSequenceClassifierProvider;
import com.berico.clavin.nerd.NerdLocationExtractor;
import com.berico.clavin.nerd.SequenceClassifierProvider;
import com.berico.clavin.resolver.LocationResolver;
import com.berico.clavin.resolver.ResolutionContext;
import com.berico.clavin.resolver.ResolvedCoordinate;
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
import org.apache.lucene.queryparser.classic.ParseException;
import org.geotools.filter.FilterFactoryImpl;
import org.jivesoftware.smack.packet.Message;
import org.opengis.filter.Filter;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;


public class XmppInputTransformer implements InputTransformer {

	private static final Logger log = Logger.getLogger(XmppInputTransformer.class);
	  private static final String CONTENT_TYPE = "text/xml";
	  public static final SimpleDateFormat ISO_8601_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	

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
	  public List<Metacard> transform(Message message) throws Exception {
	    return transform(message, null);
	  }


	  public List<Metacard> transform(Message message, String id) throws IOException, CatalogTransformerException, Exception {
	    log.info("Received chat message beginning transformation");
	    List<Metacard> metacards = new ArrayList<Metacard>();
	    if (message == null) {
	      throw new CatalogTransformerException("Cannot transform null input.");
	    }
	    log.info(message.getBody());
	    ResolutionContext places = getResolvedLocationsForString(StringEscapeUtils.escapeXml(message.getBody().replaceAll("[^a-zA-Z0-9]+", " ")));
	    
	    
	    log.info("number of places is :"+places.getLocations().size());
	    
	    if(places.getLocations()!=null && places.getLocations().size()>0){
	    for(int i=0; i<places.getLocations().size();i++){
	    	ResolvedLocation place = places.getLocations().get(i);
	    	ResolvedCoordinate coor = places.getCoordinates().get(i);
	    	
	    	Message newEntry = new Message();
		      if(!isEntryInCatalog(message, place)){
		    	  newEntry.setBody(message.getBody());
		    	  newEntry.setFrom(message.getFrom());
		    	  newEntry.setPacketID(message.getPacketID());
		    	  if(message.getSubject()!=null && message.getSubject()!=""){
		    	  newEntry.setSubject(message.getSubject());
		    	  }
		    	  else
		    	  {
		    		  newEntry.setSubject(StringEscapeUtils.escapeXml(message.getBody()));
		    	  }
		    	  newEntry.setThread(message.getThread());
		    	  newEntry.setTo(message.getTo());
		    	  newEntry.setType(message.getType());
		      }
		      else{
		    	  newEntry = null;
		      }
		      

		      MetacardImpl metacard = new MetacardImpl();
		      metacard.setTitle(newEntry.getSubject());

		      metacard.setContentTypeName(CONTENT_TYPE);
		      
		      try{
		        metacard.setThumbnail(IOUtils.toByteArray(new FileInputStream((this.getClass().getResource("/images/nyt.ico").getFile()))));
		      } catch (Exception e) {
		        metacard.setThumbnail(IOUtils.toByteArray((FrameworkUtil.getBundle(XmppInputTransformer.class).getResource("images/nyt.ico").openConnection().getInputStream())));
		      }
		      

		      
		      if(place!=null){
		        metacard.setLocation(WKTWriter.toPoint(new Coordinate(coor.getOccurrence().convertToLatLon().getLongitude(), coor.getOccurrence().convertToLatLon().getLatitude())));
		        log.info(metacard.getLocation());
		      }
		      metacard.setMetadata("<?xml version=\"1.0\"?>\n<metadata>\n" +
		              "<title>\n" + newEntry.getSubject() + "\n</title>\n" +
		              "<description>\n" + StringEscapeUtils.escapeXml(newEntry.getBody()) + "\n</description>\n" +
		              "<hashcode>" +getHashCode(newEntry.getBody()+" - "+place.getMatchedName()) + "</hashcode>\n" +
		              "</metadata>");
		      log.info("Metacard " + metacard.getTitle() + "(" + metacard.getId() + ")" + " created");
		      log.info("Metacard " + metacard.getMetadata() );
		      metacards.add(metacard);
	    	
	    }
	        return metacards;

	  }else
	  {
		  Message newEntry = new Message();
	      if(!isEntryInCatalog(message, null)){
	    	  newEntry.setBody(message.getBody());
	    	  newEntry.setFrom(message.getFrom());
	    	  newEntry.setPacketID(message.getPacketID());
	    	  if(message.getSubject()!=null && message.getSubject()!=""){
	    	  newEntry.setSubject(message.getSubject());
	    	  }
	    	  else
	    	  {
	    		  newEntry.setSubject(StringEscapeUtils.escapeXml(message.getBody()));
	    	  }
	    	  newEntry.setThread(message.getThread());
	    	  newEntry.setTo(message.getTo());
	    	  newEntry.setType(message.getType());
	      }
	      else{
	    	  newEntry = null;
	      }
	      

	      MetacardImpl metacard = new MetacardImpl();
	      metacard.setTitle(newEntry.getSubject());

	      metacard.setContentTypeName(CONTENT_TYPE);
	      
	      try{
	        metacard.setThumbnail(IOUtils.toByteArray(new FileInputStream((this.getClass().getResource("/images/nyt.ico").getFile()))));
	      } catch (Exception e) {
	        metacard.setThumbnail(IOUtils.toByteArray((FrameworkUtil.getBundle(XmppInputTransformer.class).getResource("images/nyt.ico").openConnection().getInputStream())));
	      }
	      

	      
	     
	      metacard.setMetadata("<?xml version=\"1.0\"?>\n<metadata>\n" +
	              "<title>\n" + newEntry.getSubject() + "\n</title>\n" +
	              "<description>\n" + StringEscapeUtils.escapeXml(newEntry.getBody()) + "\n</description>\n" +
	              "<hashcode>" +getHashCode(newEntry.getBody()+" - "+null) + "</hashcode>\n" +
	              "</metadata>");
	      log.info("Metacard " + metacard.getTitle() + "(" + metacard.getId() + ")" + " created");
	      log.info("Metacard " + metacard.getMetadata() );
	      metacards.add(metacard);
	  		
		return metacards;
	  }
	  }

	  @SuppressWarnings("static-access")
	private ResolutionContext getResolvedLocationsForString(String body){
		  ResolutionContext result = null;
		  
		  log.info("Here comes the classifier");
	      	
	      	SequenceClassifierProvider sequenceClassifierProvider = 
	      		    new ExternalSequenceClassifierProvider("/opt/CLAVIN-NERD/src/main/resources/all.3class.distsim.crf.ser.gz");
	      	
	      	
	      	LocationExtractor locationExtractor = null;
				try {
					locationExtractor = new NerdLocationExtractor(sequenceClassifierProvider);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
								log.info(e1);				

				}
				
					


	      		
	      	GeoParser geoParser = null;
				try {
					log.info("Before GeoParser");
					log.info("LocationExtrator is: "+locationExtractor);
			      	if(locationExtractor!=null){
			      		GeoParserFactory factory = new GeoParserFactory();
			      		factory.setLocationExtractor(locationExtractor);
			      		
			      	
					geoParser = factory.getDefault("/opt/CLAVIN/IndexDirectory");
					
					log.info("After GeoParser");}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					log.info("Exception Message is: "+e1.getMessage());				
				}
					log.info("GeoParser :"+ geoParser);
	      		
				
			  
			  
		    try {
		    	log.info("Before the parse");
		    	result = geoParser.parse(WordUtils.capitalize(body));
		    	log.info("result is: "+result.getLocations());
		    }
		    catch (ParseException e) {
		   	      log.error(e.getMessage());
		   	    }  	    
		    catch (IOException e) {
		   	      log.error(e.getMessage());
		   	    }  	      
		     catch (Exception e) {
		      log.error(e.getMessage());
		    }
	    return result;
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

	  private boolean isEntryInCatalog(Message entry, ResolvedLocation place) throws UnsupportedQueryException, SourceUnavailableException, FederationException, NoSuchAlgorithmException {
	    FilterFactoryImpl filterFactory = new FilterFactoryImpl() ;
	    Filter filter = filterFactory.like(filterFactory.property(Metacard.ANY_TEXT), getHashCode(StringEscapeUtils.escapeXml(entry.getBody()+" - "+place.getMatchedName())));
	    Query query = new QueryImpl(filter);

	    QueryRequest request = new QueryRequestImpl(query);
	    if(catalog==null){
	    	log.info("Catalog is null");
	    	
	    }
	    QueryResponse response = catalog.query(request);
	    log.info("found " + response.getResults().size() + " with " + entry.getBody().replaceAll("[^a-zA-Z0-9]+", " ") + " " + getHashCode(entry.getBody()+" - "+place.getMatchedName()));
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
