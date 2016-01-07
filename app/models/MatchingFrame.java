package models;



public class MatchingFrame {
	
	
	private String frameId;
	private String frameLabel;
	private double frameScore ;
	private String frameDescription;
	private String arg1;
	private double arg1_score;
	private String arg1Description;
	private String arg2Description;
	private String arg2;
	private double arg2_score;
	
	
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
	
	

	public double getFrameScore() {
		return frameScore;
	}
	public void setFrameScore(double frameScore) {
		this.frameScore = frameScore;
	}
	public double getArg1_score() {
		return arg1_score;
	}
	public void setArg1_score(double arg1_score) {
		this.arg1_score = arg1_score;
	}
	public double getArg2_score() {
		return arg2_score;
	}
	public void setArg2_score(double arg2_score) {
		this.arg2_score = arg2_score;
	}


}
