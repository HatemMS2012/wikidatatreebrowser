package controllers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.common.io.Files;

import models.EntityEnrty;
import models.User;
import hms.wikidata.dbimport.JacksonDBAPI;
import hms.wikidata.dbimport.WikidataToRDB;
import hms.wikidata.graph.Visualizer;
import play.*;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.*;
import views.html.*;
import static play.data.Form.*;


public class Application extends Controller {

	
	
	  public Result getd3TreeProp(String itemId,String lang, int depth,String properties) {

			List<String> targetProperties = new ArrayList<String>();
			String[] propArr = properties.split(",");
			for(String prop:propArr){
				targetProperties.add(prop);	
			}
			String repsonse = Visualizer.generateTreeForEntity(itemId, depth, lang, targetProperties);
			
			try{
				FileUtils.writeStringToFile( new File("public/javascripts/flare.json"), repsonse,"UTF-8");
			}
			catch(IOException e){
				e.printStackTrace();
			}
		  
		  return ok(tree.render(repsonse));
	  }
	  
	  public Result getd3TreeNoProp(String itemId,String lang, int depth) {
		  
			String repsonse = Visualizer.generateTreeForEntity(itemId, depth, lang, null);
			
			try{
				FileUtils.writeStringToFile( new File("public/javascripts/flare.json"), repsonse,"UTF-8");
			}
			catch(IOException e){
				e.printStackTrace();
			}
		 
		  return ok(tree.render(repsonse));
	  }
	  
	  public Result getVisTree(String itemId,String lang, int depth) {
		  

			String repsonse = Visualizer.generateCodeForVis(itemId, depth, lang, null);
			
			return ok(treeVis.render(repsonse.replace("\"", "")));
	  }
	  
	  public Result getVisTreeProp(String itemId,String lang, int depth,String properties) {
		  

		  List<String> targetProperties = new ArrayList<String>();
			String[] propArr = properties.split(",");
			for(String prop:propArr){
				targetProperties.add(prop);	
			}
			
			
			String repsonse = Visualizer.generateCodeForVis(itemId, depth, lang, targetProperties);
			
			if(repsonse==null){
				return ok("No data about: " + itemId);
			}
			
			return ok(treeVis.render(repsonse.replace("\"", "")));
	  }
	  
	  public static Form<EntityEnrty> searchForm = form(EntityEnrty.class);

	
	  public Result submit(){
		  
		  
		  DynamicForm dynamicForm = form().bindFromRequest();
		  String entityId =  dynamicForm.get("entityId").toUpperCase().trim();
		  String lang =  dynamicForm.get("language").toLowerCase().trim();
		  int depth =  Integer.valueOf(dynamicForm.get("depth").toLowerCase().trim());
		  String propsStr =  dynamicForm.get("props").replace(" ", "").toUpperCase();
		  String visMethod =  dynamicForm.get("visMethod").toLowerCase().trim();

		  		
		  if(propsStr == null ||propsStr.equals("") || propsStr.length() <= 1){
				if(visMethod.equals("d3")){
					return getd3TreeNoProp(entityId, lang, depth);
				}	
				
				else {
					return getVisTree(entityId, lang, depth);
				}
			
		  }
		  else{
			  
			  if(visMethod.equals("d3")){
				  return getd3TreeProp(entityId, lang, depth, propsStr);
			  }
			  else{
				  return getVisTreeProp(entityId, lang, depth, propsStr);
			  }
			}
			
		  
	
	  }


	  public Result index(){
		  return ok(submit.render());
	  }
	
	  
	 
	
}
