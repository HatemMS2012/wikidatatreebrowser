package controllers;

import static play.data.Form.form;
import framenet.FrameNetAPI;
import hms.alignment.framenet.xml.FrameNetXMLAPI;
import hms.wikidata.dbimport.JacksonDBAPI;
import hms.wikidata.graph.Visualizer;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.SearchFormEntity;
import models.MatchingFrame;
import models.WDEntity;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import database.WDFNMAppingDBAPI;
import views.html.*; //very important to get the project compiled

public class Application extends Controller {

	
	private static WDEntity searchedEntity;
	
	// inverse of , equivalent of, subproperty of and instance of
	private static String propertyStructureAttributes = "P1696,P1628,P1647,P31,P279,P1855,P1659,P1629,P1646";
	// instance of, subclass of
	private static String itemStructureAttributes = "P31,P279";
	
	private static final int MAX_SEMANTIC_TYPES = 10;
	private static Map<String, Integer> arg1SemanticTypes = null; // Semantic types of property arguments
	private static Map<String, Integer> arg2SemanticTypes = null; // Semantic types of property arguments
	private static List<MatchingFrame> propMatchingFrames = null; // A set of frames matching a given property
	private static List<String> availableLanguage = JacksonDBAPI.getWikidataLanguages(); // List of available languages on WD
	// The main search for
	public static Form<SearchFormEntity> searchForm = form(SearchFormEntity.class);

	/**
	 * Visualized WD proeprty according to d3 style
	 * 
	 * @param itemId
	 * @param lang
	 * @param depth
	 * @param properties
	 * @param useInstance
	 * @return
	 */
	public Result getd3TreeProp(String itemId, String lang, int depth,
			String properties, boolean useInstance) {

		Set<String> targetProperties = new HashSet<String>();
		String[] propArr = properties.split(",");
		for (String prop : propArr) {
			targetProperties.add(prop);
		}
		String repsonse = Visualizer.generateTreeForEntity(itemId, depth, lang,
				targetProperties, useInstance);

		if (repsonse == null) {
			return ok("No data about: " + itemId);
		}

		try {
			FileUtils.writeStringToFile(new File(
					"public/javascripts/flare.json"), repsonse, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ok(tree.render(repsonse));
	}

	/**
	 * Visualize WD item using d3 style
	 * 
	 * @param itemId
	 * @param lang
	 * @param depth
	 * @param useInstance
	 * @return
	 */
	public Result getd3TreeNoProp(String itemId, String lang, int depth,
			boolean useInstance) {

		String repsonse = Visualizer.generateTreeForEntity(itemId, depth, lang,
				null, useInstance);
		if (repsonse == null) {
			return ok("No data about: " + itemId);
		}

		try {
			FileUtils.writeStringToFile(new File(
					"public/javascripts/flare.json"), repsonse, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ok(tree.render(repsonse));
	}

	/**
	 * Visualize WD item using VIS library
	 * 
	 * @param itemId
	 * @param lang
	 * @param depth
	 * @param useInstance
	 * @return
	 */
	public Result getVisTree(String itemId, String lang, int depth,
			boolean useInstance) {

		String repsonse = Visualizer.generateCodeForVis(itemId, depth, lang,null, useInstance);

		if (repsonse == null) {
			return ok("No data about: " + itemId);
		}

		return ok(treeVis.render(repsonse.replace("\"", ""), searchedEntity,arg1SemanticTypes, arg2SemanticTypes, propMatchingFrames));
	}

	/**
	 * Visualize WD property using VIS library
	 * 
	 * @param itemId
	 * @param lang
	 * @param depth
	 * @param properties
	 * @param useInstance
	 * @return
	 */
	public Result getVisTreeProp(String itemId, String lang, int depth,String properties, boolean useInstance) {

		Set<String> targetProperties = new HashSet<String>();
		String[] propArr = properties.split(",");
		for (String prop : propArr) {
			targetProperties.add(prop);
		}
		String repsonse = Visualizer.generateCodeForVis(itemId, depth, lang,
				targetProperties, useInstance);

		if (repsonse == null) {
			return ok("No data about: " + itemId);
		}

		return ok(treeVis.render(repsonse.replace("\"", ""), searchedEntity,arg1SemanticTypes, arg2SemanticTypes, propMatchingFrames));
	}

	

	/**
	 * This method does the actual search
	 * 
	 * @return
	 */
	public Result searchWikiData() {

		// Get the input values from the search form
		DynamicForm dynamicForm = form().bindFromRequest();

		String entityId = dynamicForm.get("entityId").toUpperCase().trim();
		String lang = dynamicForm.get("language").toLowerCase().trim();
		String depthAsStr = dynamicForm.get("depth").toLowerCase().trim();
		int depth = 0;
		if (StringUtils.isNumeric(depthAsStr)) {
			depth = Integer.valueOf(depthAsStr);
		} else {
			return ok("Depth must be a number");
		}

		String propsStr = getPropertyList(dynamicForm.get("props"));
		String visMethod = dynamicForm.get("visMethod").toLowerCase().trim();

		boolean useInstance = false;

		if (dynamicForm.get("instance") != null) {
			useInstance = true;
			if (entityId.contains("Q")) {

				return ok("You can only use properties when the instance box is checked");
			}

		}

		// Check if the entity is a valid wikidata item
		entityId = getEntityId(entityId);

		if (entityId == null) {
			return ok("Wikidata does not contain such entity [" + entityId
					+ "]");

		}

		searchedEntity = new WDEntity();
		searchedEntity.setId(entityId);
		searchedEntity.setLabel(JacksonDBAPI.getItemLabel(entityId, lang));
		searchedEntity.setDescription(JacksonDBAPI.getItemDescription(entityId,
				lang));
		searchedEntity.setAliases(JacksonDBAPI.getItemAliases(entityId, lang));
		initPropertyArgsAndFrameMatching(entityId);

		return forwordSeachRequest(entityId, lang, depth, propsStr, visMethod,
				useInstance);

	}

	/**
	 * Forward the search request to the right viewer
	 * 
	 * @param entityId
	 * @param lang
	 * @param depth
	 * @param propsStr
	 * @param visMethod
	 * @param useInstance
	 * @return
	 */
	private Result forwordSeachRequest(String entityId, String lang, int depth,
			String propsStr, String visMethod, boolean useInstance) {
		if (propsStr == null || propsStr.equals("") || propsStr.length() <= 1) {
			if (visMethod.equals("d3")) {
				return getd3TreeNoProp(entityId, lang, depth, useInstance);
			}

			else {
				return getVisTree(entityId, lang, depth, useInstance);
			}

		} else {

			if (visMethod.equals("d3")) {
				return getd3TreeProp(entityId, lang, depth, propsStr,
						useInstance);
			} else {
				return getVisTreeProp(entityId, lang, depth, propsStr,
						useInstance);
			}
		}
	}

	/**
	 * Initialized the list of argument types and matching frames for a property
	 * 
	 * @param entityId
	 */
	private void initPropertyArgsAndFrameMatching(String entityId) {
		if (entityId.startsWith("P")) {

			// Get the experimental argument types

			arg1SemanticTypes = WDFNMAppingDBAPI.getTopNFromMap(
					WDFNMAppingDBAPI.getPropertyArgumentSemanticTypes(entityId,
							"ARG1", 1), MAX_SEMANTIC_TYPES, 1);
			arg2SemanticTypes = WDFNMAppingDBAPI.getTopNFromMap(
					WDFNMAppingDBAPI.getPropertyArgumentSemanticTypes(entityId,
							"ARG2", 1), MAX_SEMANTIC_TYPES, 1);

			// Matching Frame
			propMatchingFrames = WDFNMAppingDBAPI
					.getExperimentalFNAlignment(entityId);

		}
	}

	/**
	 * Get the set of properties that should be considered by the visualization
	 * 
	 * @param propsStr
	 * @return
	 */
	private String getPropertyList(String propsStr) {
		if (propsStr.equals("propStructure")) {
			propsStr = propertyStructureAttributes;
		} else if (propsStr.equals("classStructure")) {
			propsStr = itemStructureAttributes;
		} else if (propsStr.equals("all")) {

			propsStr = null;
		}
		return propsStr;
	}

	/**
	 * Check the entered query, it can be either an entity name or an entity
	 * label
	 * 
	 * @param entityId
	 * @return
	 */
	private String getEntityId(String entityId) {

		String res = JacksonDBAPI.getItemLabel(entityId, "en");

		if (res == null) {

			// May be the user entered a label instead of id

			List<String> candidates = JacksonDBAPI.getItemByLable(entityId,
					"en");

			if (candidates.size() > 0) {

				entityId = candidates.get(0);

				return entityId;
			} else {
				return null;

			}

		}
		return entityId;
	}

	public Result fn() {
		return ok(framenet.render());

	}

	public Result visualizeFrameNet() {

		DynamicForm dynamicForm = form().bindFromRequest();

		String frameId = dynamicForm.get("frameId");

//		List<String> matchingFrames = FrameNetXMLAPI.getFrameNetInstance().getFrame(frameId);
//		
//		
//		if (matchingFrames.size() == 0) {
//			return ok("No matching frame for your query: " + frameId);
//
//		} 

		boolean displayFrameRelations = dynamicForm.get("frameRelation") == null ? false
				: true;
		boolean displayFrameLUs = dynamicForm.get("frameLUs") == null ? false
				: true;
		boolean displaySemanticArguments = dynamicForm.get("frameSemArguments") == null ? false
				: true;

		if (!displayFrameLUs && !displayFrameRelations	&& !displaySemanticArguments) {

			return ok("Select something to display about the frame, e.g., frame relations or semantic arguments:");

		}

		String response = FrameNetAPI.generateVisualiuationCode(frameId, displayFrameLUs,	displaySemanticArguments, displayFrameRelations);

		return ok(treeVisFrame.render(response.replace("\"", "")));

	}

	public Result index() {
		return ok(submit.render(availableLanguage));
	}


}
