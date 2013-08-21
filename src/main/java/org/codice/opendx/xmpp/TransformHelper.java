package org.codice.opendx.xmpp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;

import com.berico.clavin.GeoParser;
import com.berico.clavin.Options;
import com.berico.clavin.extractor.LocationExtractor;
import com.berico.clavin.extractor.coords.DdHemiLetterPatternParsingStrategy;
import com.berico.clavin.extractor.coords.DdPatternParsingStrategy;
import com.berico.clavin.extractor.coords.DmsPatternParsingStrategy;
import com.berico.clavin.extractor.coords.RegexCoordinateExtractor;
import com.berico.clavin.extractor.coords.RegexCoordinateParsingStrategy;
import com.berico.clavin.nerd.ExternalSequenceClassifierProvider;
import com.berico.clavin.nerd.NerdLocationExtractor;
import com.berico.clavin.nerd.SequenceClassifierProvider;
import com.berico.clavin.resolver.LocationResolver;
import com.berico.clavin.resolver.ResolutionContext;
import com.berico.clavin.resolver.impl.CoordinateCandidateSelectionStrategy;
import com.berico.clavin.resolver.impl.CoordinateIndex;
import com.berico.clavin.resolver.impl.DefaultLocationResolver;
import com.berico.clavin.resolver.impl.LocationCandidateSelectionStrategy;
import com.berico.clavin.resolver.impl.LocationNameIndex;
import com.berico.clavin.resolver.impl.ResolutionResultsReductionStrategy;
import com.berico.clavin.resolver.impl.lucene.LuceneComponents;
import com.berico.clavin.resolver.impl.lucene.LuceneComponentsFactory;
import com.berico.clavin.resolver.impl.lucene.LuceneCoordinateIndex;
import com.berico.clavin.resolver.impl.lucene.LuceneLocationNameIndex;
import com.berico.clavin.resolver.impl.strategies.IdentityReductionStrategy;
import com.berico.clavin.resolver.impl.strategies.WeightedCoordinateScoringStrategy;
import com.berico.clavin.resolver.impl.strategies.coordinates.ResolvedCoordinateWeigher;
import com.berico.clavin.resolver.impl.strategies.coordinates.SharedLocationNameWeigher;
import com.berico.clavin.resolver.impl.strategies.coordinates.VectorDistanceWeigher;
import com.berico.clavin.resolver.impl.strategies.locations.ContextualOptimizationStrategy;

public class TransformHelper {
	private static final Logger log = Logger.getLogger(TransformHelper.class);
	
	public ResolutionContext getResolvedLocationsForString(String body){
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
			      		
			      		
			      		
			      		List<RegexCoordinateParsingStrategy<?>> 
			      		  		DefaultCoordinateParsingStrategies = 
			      		  		new ArrayList<RegexCoordinateParsingStrategy<?>>();
			      		
			      		List<ResolvedCoordinateWeigher> 
			      		 		DefaultCoordinateWeighers = new ArrayList<ResolvedCoordinateWeigher>();
			      		
			      		DefaultCoordinateParsingStrategies.add(new DmsPatternParsingStrategy());
			      		DefaultCoordinateParsingStrategies.add(new DdPatternParsingStrategy());
			      		DefaultCoordinateParsingStrategies.add(new DdHemiLetterPatternParsingStrategy());
			      		
			      		DefaultCoordinateWeighers.add(new SharedLocationNameWeigher());
			      		DefaultCoordinateWeighers.add(new VectorDistanceWeigher());
			      		
			      		RegexCoordinateExtractor coordinateExtractor = 
			      				 				new RegexCoordinateExtractor(DefaultCoordinateParsingStrategies);
			      		
			      		Options options = new Options();
			      		int maxHitDepth = 1;
			      		boolean fuzzy = false;
			      		int maxContentWindow = 1;
			      		LuceneLocationNameIndex.configureLimit(options, maxHitDepth);
			      		LuceneLocationNameIndex.configureUseFuzzy(options, fuzzy);
			      		ContextualOptimizationStrategy.configureMaxContextWindow(options, maxContentWindow);
			      		LuceneComponentsFactory factory2 = new LuceneComponentsFactory("/opt/CLAVIN/IndexDirectory");
			      		 		
			      		 		factory2.initializeSearcher();
			      		 		
			      		 		
			      		 		LuceneComponents lucene = factory2.getComponents();
			      		 		
			      		 		// Instantiate the Indexes.
			      		 		LocationNameIndex locationNameIndex = new LuceneLocationNameIndex(lucene);
			      		 		
			      		 		CoordinateIndex coordinateIndex = new LuceneCoordinateIndex(lucene);
			      		 		
			      		 		// Instantiate the resolution strategies
			      		 		CoordinateCandidateSelectionStrategy coordinateSelectionStrategy = 
			      		 			new WeightedCoordinateScoringStrategy(DefaultCoordinateWeighers);
			      		 		
			      		 		LocationCandidateSelectionStrategy locationSelectionStrategy = 
			      		 				new ContextualOptimizationStrategy();
			      		 		
			      		 		// Instantiate the reducer
			      		 		ResolutionResultsReductionStrategy reductionStrategy = 
			      		 				new IdentityReductionStrategy();
			      		 		
			      		 		// Instantiate the LocationResolver
			      		 		LocationResolver resolver = new DefaultLocationResolver(
			      		 				locationNameIndex, 
			      		 				coordinateIndex, 
			      		 				locationSelectionStrategy, 
			      		 				coordinateSelectionStrategy, 
			      		 				reductionStrategy);
			      		
			      		
			      						
					geoParser = new GeoParser(locationExtractor,coordinateExtractor,resolver);
					
					log.info("After GeoParser");}
				
		    	log.info("Before the parse: "+ body);
		    	result = geoParser.parse(WordUtils.capitalize(body));
		    	log.info(result.getLocations());
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
}
