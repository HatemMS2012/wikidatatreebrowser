package controllers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.google.common.io.Files;

import models.EntityEnrty;
import models.MatchingFrame;
import models.User;
import hms.alignment.FrameNetAPI;
import hms.alignment.data.Frame;
import hms.alignment.data.SemanticRole;
import hms.wikidata.dbimport.JacksonDBAPI;
import hms.wikidata.dbimport.WikidataToRDB;
import hms.wikidata.graph.Visualizer;
import hms.wikidata.model.ExperimentalArgTypes;
import play.*;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.*;
import views.html.*;
import static play.data.Form.*;


public class Application extends Controller {
	
	
	

		
	public static List<String> availableLanguage = JacksonDBAPI.getWikidataLanguages();
	
	  public Result getd3TreeProp(String itemId,String lang, int depth,String properties,boolean useInstance) {

			Set<String> targetProperties = new HashSet<String>();
			String[] propArr = properties.split(",");
			for(String prop:propArr){
				targetProperties.add(prop);	
			}
			String repsonse =	Visualizer.generateTreeForEntity(itemId, depth, lang, targetProperties,useInstance);
			
			if(repsonse==null){
				return ok("No data about: " + itemId);
			}
			
			try{
				FileUtils.writeStringToFile( new File("public/javascripts/flare.json"), repsonse,"UTF-8");
			}
			catch(IOException e){
				e.printStackTrace();
			}
		  
		  return ok(tree.render(repsonse));
	  }
	  
	  public Result getd3TreeNoProp(String itemId,String lang, int depth, boolean useInstance) {
		  
			String repsonse = Visualizer.generateTreeForEntity(itemId, depth, lang, null,useInstance);
			if(repsonse==null){
				return ok("No data about: " + itemId);
			}
			
			try{
				FileUtils.writeStringToFile( new File("public/javascripts/flare.json"), repsonse,"UTF-8");
			}
			catch(IOException e){
				e.printStackTrace();
			}
		 
		  return ok(tree.render(repsonse));
	  }
	  
	  public Result getVisTree(String itemId,String lang, int depth,boolean useInstance) {
		  

			String repsonse = Visualizer.generateCodeForVis(itemId, depth, lang, null,useInstance);
			if(repsonse==null){
				return ok("No data about: " + itemId);
			}
				
			return ok(treeVis.render(repsonse.replace("\"", ""),PROPETY_ARG_TYPES.getId(),PROPETY_ARG_TYPES.getLabel(), PROPETY_ARG_TYPES.getDescription(), PROPETY_ARG_TYPES.getTypeArg1(), PROPETY_ARG_TYPES.getTypeArg2(),propMatchingFrames));
	  }
	  
	  public Result getVisTreeProp(String itemId,String lang, int depth,String properties,boolean useInstance) {
		  

		  Set<String> targetProperties = new HashSet<String>();
			String[] propArr = properties.split(",");
			for(String prop:propArr){
				targetProperties.add(prop);	
			}
			String repsonse =  Visualizer.generateCodeForVis(itemId, depth, lang, targetProperties,useInstance);
			
			if(repsonse==null){
				return ok("No data about: " + itemId);
			}
			
			return ok(treeVis.render(repsonse.replace("\"", ""),PROPETY_ARG_TYPES.getId(),PROPETY_ARG_TYPES.getLabel(), PROPETY_ARG_TYPES.getDescription(), PROPETY_ARG_TYPES.getTypeArg1(), PROPETY_ARG_TYPES.getTypeArg2(),propMatchingFrames));
	  }
	  
	  public static Form<EntityEnrty> searchForm = form(EntityEnrty.class);

	
	  static ExperimentalArgTypes PROPETY_ARG_TYPES = null;
	  static List<MatchingFrame> propMatchingFrames = null;
	  static final String frameMatchingSimMethod = "WN"; //stemoverlap
	  
	  public Result submit(){
		  
		  
		  DynamicForm dynamicForm = form().bindFromRequest();
		  
		  String entityId =  dynamicForm.get("entityId").toUpperCase().trim();
		  
		
		  
		  if(entityId.startsWith("P")){
			  
			  //Get the experimental argument types
			 PROPETY_ARG_TYPES = JacksonDBAPI.getExperimentalArgTypes(entityId);
			 
			 //Matching Frame
			 
			 propMatchingFrames = getMatchingFrames(entityId,frameMatchingSimMethod);
			 
			  
		  }
		  String lang =  dynamicForm.get("language").toLowerCase().trim();
		  
		  try{
			  Integer.valueOf(dynamicForm.get("depth").toLowerCase().trim());
		  }
		  catch(NumberFormatException e){
			  return ok("Depth must be a number");
		  }
		  int depth =  Integer.valueOf(dynamicForm.get("depth").toLowerCase().trim());
		  
		  String propsStr =  dynamicForm.get("props");
		 
		  if(propsStr.equals("propStructure")){
			  propsStr = propertyStructureAttributes;
		  }
		  else if(propsStr.equals("classStructure")){
			  propsStr = itemStructureAttributes;
		  }	
		  else if(propsStr.equals("all")){
			  
			  propsStr = null;
		  }
		 
		  		  
		  String visMethod =  dynamicForm.get("visMethod").toLowerCase().trim();
		  boolean useInstance = false;
		  
		  //Check if the entity is a valid wikidata item
		  String res = JacksonDBAPI.getItemLabel(entityId, "en");
		  if(res== null) {
			  return ok("Wikidata does not contain such entity [" + entityId + "]");

		  }
		  if(dynamicForm.get("instance")!=null){
			  useInstance = true;
			  
			  if(entityId.contains("Q")){
				
				  return ok("You can only use properties when the instance box is checked");
			  }

		  }
		  		
		  if(propsStr == null ||propsStr.equals("") || propsStr.length() <= 1){
				if(visMethod.equals("d3")){
					return getd3TreeNoProp(entityId, lang, depth,useInstance);
				}	
				
				else {
					return getVisTree(entityId, lang, depth,useInstance);
				}
			
		  }
		  else{
			  
			  if(visMethod.equals("d3")){
				  return getd3TreeProp(entityId, lang, depth, propsStr,useInstance);
			  }
			  else{
				  return getVisTreeProp(entityId, lang, depth, propsStr,useInstance);
			  }
			}
			
		  
	
	  }


	  /**
	   * Get frames matching a given WD property
	   * @param entityId
	   * @param method
	   * @return
	   */
	  private List<MatchingFrame> getMatchingFrames(String entityId,String method) {
		  
		  List<MatchingFrame> matchingFrames = new ArrayList<MatchingFrame>();
		 
		  List<String> fnMatching = JacksonDBAPI.getExperimentalFNAlignment(entityId,method);
	
		  
		  for(String match : fnMatching){
			  MatchingFrame matchingFrame = new MatchingFrame();
			  
			  String frameId = match.split(":")[0];
			  Frame fullFrame = FrameNetAPI.getFrameFullData(frameId);
			  
			  String arg1 = match.split(":")[1].split("#")[0];
			  String arg2 = match.split(":")[1].split("#")[1];
				 
			  matchingFrame.setFrameId(frameId);
			  matchingFrame.setFrameLabel(fullFrame.getLabel());
			  matchingFrame.setFrameDescription(fullFrame.getDefinition());
			  matchingFrame.setArg1(arg1);
			  matchingFrame.setArg2(arg2);
			  
			  //Extract argument descriptions
			  matchingFrames.add(matchingFrame);
		  }
			
		return matchingFrames;
	}
	  
	
	public Result fn(){
		  return ok(framenet.render());

	  }
	  public Result visualizeFrameNet(){
		 
		  DynamicForm dynamicForm = form().bindFromRequest();
		  
		  
		  String frameId = dynamicForm.get("frameId");
		  
		  List<String> matchingFrames = FrameNetAPI.getFrameByLabel(frameId, true);
		  if(matchingFrames.size()==0){
			  return ok("No matching frame for your query: " + frameId);
			  
		  }
		  else{
			  frameId = matchingFrames.get(0);
		  }
		  
		  boolean displayFrameRelations = dynamicForm.get("frameRelation")==null?false:true;
		  boolean displayFrameLUs = dynamicForm.get("frameLUs")==null?false:true;
		  boolean displaySemanticArguments = dynamicForm.get("frameSemArguments")==null?false:true;
		  
		  if(!displayFrameLUs && !displayFrameRelations && !displaySemanticArguments){
			  
			  return ok("Select something to display about the frame, e.g., frame relations or semantic arguments:");

		  }
		  
		  String response = FrameNetAPI.generateVisCode(frameId, displayFrameLUs, displaySemanticArguments, displayFrameRelations);
		  
		  
		  return ok(treeVis.render(response.replace("\"", ""),PROPETY_ARG_TYPES.getId(),PROPETY_ARG_TYPES.getLabel(), PROPETY_ARG_TYPES.getDescription(), PROPETY_ARG_TYPES.getTypeArg1(), PROPETY_ARG_TYPES.getTypeArg2(),propMatchingFrames));
		  
	  }
	  public Result index(){
		  return ok(submit.render(availableLanguage));
	  }
	
	  

	  //inverse of , equivalent of, subproperty of and instance of
	 private static String propertyStructureAttributes = "P1696,P1628,P1647,P31,P279,P1855,P1659,P1629,P1646" ; 
	 //instance of, subclass of
	 private static String itemStructureAttributes = "P31,P279" ;
	
}
