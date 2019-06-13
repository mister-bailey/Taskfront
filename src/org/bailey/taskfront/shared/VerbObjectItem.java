package org.bailey.taskfront.shared;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bailey.taskfront.client.Database;
import org.bailey.taskfront.client.JsObject;

import com.google.gwt.user.client.Window;

public class VerbObjectItem extends ModifiedItem  {
	
	/*private ItemViewItem.FilterObserver objectObserver;
	public boolean hasObject(){return objectObserver != null;}
	public Item getObjectItem(){
		if(!hasObject())return null;
		return Database.getItem(objectObserver.itemID);
	}*/
	
	public static Set<String> verbs = new HashSet<String>();
	static{
		verbs.add("get ");
		verbs.add("buy ");
		verbs.add("talk to ");
		verbs.add("write to ");
		verbs.add("call ");
		verbs.add("see ");
		verbs.add("visit ");
		verbs.add("work on ");
	}
	public static VerbObjectItem createVerbObjectItem(String verb, String objectID){
		verb = verb.toLowerCase().trim();
		// TODO: deal with specific kinds of verbs, to create specific kinds of items.
		return new VerbObjectItem(verb, objectID);
	}

	public VerbObjectItem(){
		super();
		this.selfSave=true; // default for these
	}
	public VerbObjectItem(String verb, String objectID){
		super(verb, objectID);
		content.set("type","VerbObjectItem");
		this.selfSave=true;
	}
	
		
	public void complete(){
		double now = (new Date()).getTime();
		boolean repeated=false;
		if(hasProperty("repeatPeriod")){
			Item x = copyAndAdd(getStringProperty("repeatParent"));
			x.setTime(now + this.getDoubleProperty("repeatPeriod"));
			repeated=true;
		}
		if(hasObject()){
			Item object = getObjectItem();
			if(object.hasProperty("neededActions")){
				JsObject actions = object.content.getJsObject("neededActions");
				if(actions.has(getPrefaceText())){ // TODO: more sophisticated satisfaction check
					JsObject action = actions.getJsObject(getPrefaceText());
					if(!repeated && action.has("frequency")){
						double frequency = action.getDouble("frequency");
						Item x = this.copyAndAdd(action.getString("repeatParent"));
						x.setTime(now + frequency); // saves!
						
						// TODO: more sophisticated search for satisfied candidates
						// if(action.has("candidate")) Database.getItem(action.getString("candidate")).delete();
						
						action.set("candidate",x.uid);
						object.updateObservers();
						if(object.selfSave) object.saveContent();
					} else {
						object.removeNeededAction(getPrefaceText());
					}
				}
			}
		}
		content.set("completionTime",now);
		setIs("completed",true); //	updates and saves
	}

}
