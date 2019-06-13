package org.bailey.taskfront.shared;

public abstract class ModifiedItem extends HasOwnListItem {
	
	public ModifiedItem(){super();}
	public ModifiedItem(String modifierText, String objectID){
		super(modifierText,objectID);
	}
	
	// In this case, content.text is the modifier part only!
	// getText != reverse of setText!
	public void setModifierText(String text){setText(text);}
	public String getModifierText(){
		return super.getText();
	}
	public String getObjectText(){
		if(objectItemID == null) return null; 
		return getObjectItem().toString();
	}
	public String getVerbObjectText(){
		String objectText = getObjectText();
		return getPrefaceText() + " " + (objectText==null ? "" : objectText);
	}
	public String getText(){
		String objectText = getObjectText();
		String postscript = getPostscript();
		return getPrefaceText() + " " + (objectText==null ? "" : objectText) + (postscript==null ? "" : postscript);
	}
	public void setPostscript(String text){
		setProperty("postscript",text); // updates and saves!
	}
	public String getPostscript(){
		return getStringProperty("postscript");
	}
	public void setPrefaceText(String text){setModifierText(text);}
	public String getPrefaceText(){return getModifierText();}


}
