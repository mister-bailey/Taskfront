package org.bailey.taskfront.shared;

public class IsPropertyItem extends HasPropertyItem {

	public IsPropertyItem(String text, String filterID, String property, boolean is){
		super(text,filterID,property,is);
		content.set("type","IsPropertyItem");
	}
	public IsPropertyItem(){super();}
	
	// TODO: case for subproperties
	public boolean include(Item item){
		return has() == (item.content.has(property()) && item.content.getBoolean(property()));
	}
	public void setInclude(Item item, boolean include){
		item.setIs(property(),has()==include);
	}
	
}
