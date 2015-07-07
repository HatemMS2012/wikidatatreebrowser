package models;

import java.util.List;

public class EntityEnrty {

	
	protected  String itemId;
	protected  String lang;
	protected  List<String > propertyList;
	
	
	public String getItemId() {
		return itemId;
	}
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	public String getLang() {
		return lang;
	}
	public void setLang(String lang) {
		this.lang = lang;
	}
	public List<String> getPropertyList() {
		return propertyList;
	}
	public void setPropertyList(List<String> propertyList) {
		this.propertyList = propertyList;
	}
	
	
}
