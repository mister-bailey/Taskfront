package org.bailey.taskfront.shared;

import org.bailey.taskfront.client.Database;
import org.bailey.taskfront.client.JsObject;

public class NeededActionListItem extends FilteredListItem {
	
	public static Item frequencySequence(String filterItem, /*String repeatParent,*/ String verb, double frequency0, double... frequencies){
		Item sequence = new VerbObjectItem(verb,filterItem);//verb.substring(0,1).toUpperCase() + verb.substring(1) + " " + Database.getItem(filterItem).toString());
		sequence.selfSave=false;
		double f0 = frequency0;
		NeededActionListItem n0=null;
		for(double f1 : frequencies){
			NeededActionListItem n1 = new NeededActionListItem(filterItem, verb, f0, f1);
			if(n0 != null){
				n0.setProperty("demotionTarget",n1.uid);
				n1.setProperty("promotionTarget",n0.uid);
			}
			sequence.add(n1.uid);
			f0 = f1;
			n0 = n1;
		}		
		return sequence;		
	}
	
	public NeededActionListItem(){super();}
	public NeededActionListItem(String text, String filterItem, /*String repeatParent,*/ String verb, double minFrequency, double maxFrequency){
		super(text,filterItem);
		content.set("verb",verb);
		content.set("minFrequency",minFrequency);
		content.set("maxFrequency",maxFrequency);
		content.set("type","NeededActionListItem");
		populateUIDlist();
	}
	public NeededActionListItem(String filterItem, String verb, double minFrequency, double maxFrequency){
		this(null, filterItem, verb, minFrequency, maxFrequency);
		String text;
		if(minFrequency <= DT.dayLength && maxFrequency <= DT.dayLength) text = "daily";
		else{
			text = "every " + (int)(minFrequency / DT.dayLength) + " to " +
					(int)(maxFrequency / DT.dayLength) + " days";
		}
		content.set("text",text);
	}

	public String getVerb(){
		return getStringProperty("verb");
	}
	public double getMinFrequency(){
		return getDoubleProperty("minFrequency");
	}
	public double getMaxFrequency(){
		return getDoubleProperty("maxFrequency");
	}
	public String getActionParent(){
		return getStringProperty("actionParent");
	}
	
	public boolean include(Item item) {
		if(!item.hasProperty("neededActions")) return false;
		JsObject actions = item.content.getJsObject("neededActions");
		String verb = this.getVerb();
		if(!actions.has(verb)) return false;
		JsObject action = actions.getJsObject(verb);
		if(!action.has("frequency")) return false;
		double fq = action.getDouble("frequency");
		return fq >= getMinFrequency() && fq < getMaxFrequency();
	}

	public void setInclude(Item item, boolean include) {
		if(include){
			if(!include(item)){
				item.setNeededActionAndEnsureCandidate(getVerb(),getActionParent(),(getMinFrequency() + getMaxFrequency())/2);
			}
		} else item.removeNeededAction(getVerb());
	}

	@Override
	public double childOrder(Item child) {
		return child.content.getJsObject("neededActions").getJsObject(getVerb()).getDouble("frequency");
	}

	protected void filterUpdate() {
	}
	@Override
	public double getStartOrder() {
		return getMinFrequency();
	}
	@Override
	public double getEndOrder() {
		return getMaxFrequency();
	}
	@Override
	public void setChildOrder(Item item, double order) {
		item.setNeededActionAndEnsureCandidate(getVerb(),getActionParent(),order);
	}

}
