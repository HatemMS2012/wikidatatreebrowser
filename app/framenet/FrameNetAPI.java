package framenet;



import hms.alignment.framenet.xml.FrameNetXMLAPI;

import java.util.Collection;

import de.saar.coli.salsa.reiter.framenet.Frame;
import de.saar.coli.salsa.reiter.framenet.FrameElement;
import de.saar.coli.salsa.reiter.framenet.FrameNet;
import de.saar.coli.salsa.reiter.framenet.FrameNetRelationDirection;
import de.saar.coli.salsa.reiter.framenet.FrameNotFoundException;
import de.saar.coli.salsa.reiter.framenet.LexicalUnit;

public class FrameNetAPI {
	
	
	private static FrameNet FNACCSSEPONIT = FrameNetXMLAPI.getFrameNetInstance();

	
	/**
	 * Generate VIS code for visualizing frames
	 * @param frameId
	 * @param dispalyLU
	 * @param displaySemanticArguments
	 * @param displayFrameRelations
	 * @return
	 * @throws FrameNotFoundException 
	 */
	public static String generateVisualiuationCode(String frameId,boolean dispalyLU, boolean displaySemanticArguments, boolean displayFrameRelations) {
		
		Frame f = null;
		try {
			f = FNACCSSEPONIT.getFrame(frameId);
		} catch (FrameNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
		StringBuffer strBufferNodes = new StringBuffer();
		
		StringBuffer strBufferEdges = new StringBuffer();

		 strBufferEdges.append("var edges = new vis.DataSet([\n");
		//start
		strBufferNodes.append("var nodes = new vis.DataSet([ \n");
		
		//Nodes
		
		strBufferNodes.append("{id:1").append(", label:'").append(f.getName()).append("',color: {background:'yellow', border:'blue'}}").append(",\n");
		
		int i = 2;
		
		if(displayFrameRelations){

////			Collection<Frame> relatedFrames = f.allInheritedFrames(); 
//			Collection<Frame> relatedFrames = f.inheritsFrom();
////			relatedFrames.addAll(relatedFrames2);
//			
//			for(Frame e: relatedFrames){
//				
//				strBufferNodes.append("{id:").append(i).append(", label:'").append(f.getName()).append("'}").append(", \n");
//				strBufferEdges.append("{from:1").append(", to:").append(i).append(", label: '").append(e.getName()).append("', arrows:'to'},\n");
//				i++;
//			}
		}
		
		if(displaySemanticArguments){
		 Collection<FrameElement> semanticRoles = f.frameElements();
		
			for(FrameElement sr : semanticRoles){
				
				strBufferNodes.append("{id:").append(i).append(", label:'").append(sr.getName()).append("'}").append(", \n");
				if(sr.isCore()){

					strBufferEdges.append("{from:1").append(", to:").append(i).append(", label: '").append(sr.getCoreType()).append("', color: 'red' , arrows:'to'},\n");	
				}
				else{

					strBufferEdges.append("{from:1").append(", to:").append(i).append(", label: '").append(sr.getCoreType()).append("', arrows:'to'},\n");
				}
				i++;
			}
		}
		if(dispalyLU){
			Collection<LexicalUnit> lus = f.getLexicalUnits();
			for(LexicalUnit lu : lus){
				
				strBufferNodes.append("{id:").append(i).append(", label:'").append(lu).append("'}").append(", \n");
				strBufferEdges.append("{from:1").append(", to:").append(i).append(", label: '").append("LU").append("', arrows:'to'},\n");
				i++;
			}
		}
		//End
		strBufferNodes = new StringBuffer(strBufferNodes.toString().substring(0, strBufferNodes.toString().lastIndexOf(",")));
		
		strBufferEdges = new StringBuffer(strBufferEdges.toString().substring(0, strBufferEdges.toString().lastIndexOf(",")));
		
		strBufferNodes.append("]);");
		strBufferEdges.append("]);");

		
		return strBufferNodes.toString() + "\n" + strBufferEdges;
	}

}
