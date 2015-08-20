package models;

import hms.alignment.FrameNetAPI;
import hms.alignment.data.Frame;
import hms.alignment.data.SemanticRole;

import java.util.List;

public class MatchingFrame {
	
	private String frameId;
	private String frameLabel;
	private String frameDescription;
	private String arg1;
	private String arg1Description;
	private String arg2Description;
	private String arg2;
	private int rank;
	public String getFrameId() {
		return frameId;
	}
	public void setFrameId(String frameId) {
		this.frameId = frameId;
	}
	public String getFrameLabel() {
		return frameLabel;
	}
	public void setFrameLabel(String frameLabel) {
		this.frameLabel = frameLabel;
	}
	public String getFrameDescription() {
		return frameDescription;
	}
	public void setFrameDescription(String frameDescription) {
		this.frameDescription = frameDescription;
	}
	public String getArg1() {
		return arg1;
	}
	public void setArg1(String arg1) {
		this.arg1 = arg1;
	}
	public String getArg1Description() {
		return arg1Description;
	}
	public void setArg1Description(String arg1Description) {
		this.arg1Description = arg1Description;
	}
	public String getArg2Description() {
		return arg2Description;
	}
	public void setArg2Description(String arg2Description) {
		this.arg2Description = arg2Description;
	}
	public String getArg2() {
		return arg2;
	}
	public void setArg2(String arg2) {
		this.arg2 = arg2;
	}
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	
	
	public String getRoleDescription(String roleName){
		
		 
		Frame fullFrame = FrameNetAPI.getFrameFullData(frameId);

		List<SemanticRole> roles = fullFrame.getRoles();
		  
		  for(SemanticRole r : roles ){
			  if(r.getRole().equals(roleName.trim())){
				  return r.getDefnition();
			  }
		  }
		
		return null;
		
	}
	
	
	

}
