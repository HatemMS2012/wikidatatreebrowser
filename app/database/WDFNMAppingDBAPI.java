package database;

import hms.alignment.framenet.xml.FrameNetXMLAPI;
import hms.similarity.ValueComparatorForInt;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import models.MatchingFrame;
import de.saar.coli.salsa.reiter.framenet.Frame;
import de.saar.coli.salsa.reiter.framenet.FrameElementNotFoundException;
import de.saar.coli.salsa.reiter.framenet.FrameNet;
import de.saar.coli.salsa.reiter.framenet.FrameNotFoundException;



public class WDFNMAppingDBAPI {

	private static FrameNet FNACCSSEPONIT = FrameNetXMLAPI.getFrameNetInstance();

	
	
	private static final String SELECT_PROP_ARG_SEMANTIC_TYPES = "SELECT propId,argument,semanticTypes FROM wikidata_framenet_mapping.wd_argument_semantic_types WHERE propId = ? and argument =? ";
	private static final String SELECT_EXPRIMENTAL_FN_ALIGNMENT_FOR_PROPERTY = "SELECT * FROM wikidata_framenet_mapping.wd_fn_fe_arg_alignments where propId = ? order by prop_frame_score desc";


	
	/**
	 * Get the semantic types of a property argument
	 * @param propId
	 * @param argument
	 * @param minFreq
	 * @return
	 */
    public static Map<String, Integer> getPropertyArgumentSemanticTypes(String propId, String argument,int minFreq){
		Map<String, Integer> semanticTypes = null;
		
		PreparedStatement st = DBConnector.createPreparedStatemen(SELECT_PROP_ARG_SEMANTIC_TYPES);
		
		try {
			st.setString(1, propId);
			st.setString(2, argument);
			ResultSet result = st.executeQuery();
			if(result.next()){
				
				semanticTypes = new HashMap<String, Integer>();
				String[] sumoConceptArr = result.getString("semanticTypes").split(",");
				for(String arrElement : sumoConceptArr){

					
					if(arrElement.equals(""))
						continue;
					String[] oneEntry = arrElement.split("=");
					String concept = oneEntry[0].trim();
					int freq = Integer.valueOf(oneEntry[1].trim());
					
					if(freq>= minFreq){
						semanticTypes.put(concept, freq);
					}
					
				}
				
			}
			st.close();
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return semanticTypes;
	}
	
    

	
	
	public static Map<String, Integer> getTopNFromMap(Map<String, Integer> itemFreqMap, int maxElements, int minFreq) {
	
		if(itemFreqMap == null){
			return new HashMap<String, Integer>();
		}
		ValueComparatorForInt bvc =  new ValueComparatorForInt(itemFreqMap);
		TreeMap<String,Integer> sorted_map = new TreeMap<String,Integer>(bvc);
		sorted_map.putAll(itemFreqMap);
		
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		
		int max = Math.min(maxElements,itemFreqMap.size());
		int count = 0 ;
		
		for( Entry<String, Integer> e: sorted_map.entrySet()){
			if(count >= max){
				break;
			}
			if(e.getValue() >= minFreq){
				result.put(e.getKey(), e.getValue());
			}
			count++;
		}
		 
		return result;
		

	}
	


	/**
	 * Get property-frame alignments
	 * @param propId
	 * @return
	 */
    public static List<MatchingFrame> getExperimentalFNAlignment(String propId){
        
    	
    	PreparedStatement st;
    	List<MatchingFrame> matchingFrames = new ArrayList<MatchingFrame>();

    	try {
			st = DBConnector.createPreparedStatemen(SELECT_EXPRIMENTAL_FN_ALIGNMENT_FOR_PROPERTY);
			st.setString(1,propId);
			
			
			ResultSet result =  st.executeQuery();
			
			
			
			while(result.next()){
				
				String frameId = result.getString("frame");
				
				double frame_score = result.getDouble("prop_frame_score");
				String arg1 = result.getString("arg1");
				String arg2 = result.getString("arg2");
				
				double arg1_score = result.getDouble("arg1_score");
				double arg2_score = result.getDouble("arg2_score");
				
				MatchingFrame  f = new MatchingFrame();
				f.setFrameId(frameId);
				f.setFrameLabel(frameId);
				f.setFrameScore(frame_score);
				f.setArg1(arg1);
				f.setArg1_score(arg1_score);
				f.setArg2_score(arg2_score);
				f.setArg2(arg2);
				
				
				Frame frame = null;
				  try {
					  frame = FNACCSSEPONIT.getFrame(frameId);
					  String frameDef = frame.getDefinition();
					  f.setFrameDescription(frameDef.substring(0, frameDef.indexOf(".")));

					  f.setArg1Description(frame.getFrameElement(arg1).getDefinition());
					  f.setArg2Description(frame.getFrameElement(arg2).getDefinition());

						
				  } catch (FrameNotFoundException e) {
					  e.printStackTrace();
				  } catch (FrameElementNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				
				matchingFrames.add(f);
			}
			st.close();
    	}
    	
    	 catch (SQLException e) {
 			
 			e.printStackTrace();
 		}
    	return matchingFrames;
	}
}
