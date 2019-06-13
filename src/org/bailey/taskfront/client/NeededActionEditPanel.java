package org.bailey.taskfront.client;

import java.util.Date;

import org.bailey.taskfront.shared.DT;
import org.bailey.taskfront.shared.Item;
import org.bailey.taskfront.shared.VerbObjectItem;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class NeededActionEditPanel extends EditPanel {
	String verb;
	boolean deleted=false;

	TextBox verbBox = new TextBox();
	CheckBox repeating = new CheckBox("Repeating every ");
	DoubleBox frequency = new DoubleBox();
	ItemSelectBox parent = new ItemSelectBox();
	
	public NeededActionEditPanel(String itemID, String verb) {
		super(itemID);
		this.setStyleName("subEditPanel");
		this.verb = verb;
		this.add(verbBox);
		this.add(repeating);
		this.add(new HTML("<br>"));
		this.add(frequency);
		this.add(new HTML(" days in "));
		this.add(parent);
		parent.setUID("mainList");
		this.add(new Button("X", new ClickHandler(){
			public void onClick(ClickEvent event) {
				NeededActionEditPanel.this.deleteAction();
			}
		}));
		repeating.addValueChangeHandler(new ValueChangeHandler<Boolean>(){
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				NeededActionEditPanel.this.frequency.setEnabled(event.getValue());
				//NeededActionEditPanel.this.parent.setEnabled(event.getValue());				
			}			
		});
		
		Item item = Database.getItem(itemID);
		if(item.hasProperty("neededActions") && item.content.getJsObject("neededActions").has(verb)) update();
	}
	
	public void update(){
		if(deleted)return;
		Item item = Database.getItem(itemID);
		if(!item.hasProperty("neededActions") || !item.content.getJsObject("neededActions").has(verb)){
			this.removeFromParent();
			return;
		}
		verbBox.setText(verb);
		JsObject action = item.content.getJsObject("neededActions").getJsObject(verb);
		if(action.has("frequency")){
			repeating.setValue(true);
			frequency.setEnabled(true);
			frequency.setValue((action.getDouble("frequency") / DT.dayLength));
		} else {
			repeating.setValue(false);
			frequency.setEnabled(false);			
		}
		parent.setUID(action.getString("repeatParent"));
	}
	public void deleteAction(){
		this.deleted=true;
		this.setVisible(false);
	}
	
	// Doesn't need to save or update observers???
	public void save(){
		Item item = Database.getItem(itemID);
		String verbBoxText = verbBox.getText().toLowerCase().trim();
		if(verb==null) verb = verbBoxText;
		
		if(deleted || verbBoxText.equals("")){
			item.removeNeededAction(verb);
			return;
		}
		
		if(!item.content.has("neededActions")) item.content.set("neededActions", JsObject.newJsObject());
		
		JsObject actions = item.content.getJsObject("neededActions");
		JsObject action;
		if(actions.has(verb)) action=actions.getJsObject(verb);
		else actions.set(verb, action = JsObject.newJsObject());
		
		if(!verb.equals(verbBoxText)){
			actions.delete(verb);
			verb=verbBoxText;
			actions.set(verb,action);
		}
		
		double now = (new Date()).getTime();
		double fq = frequency.getValue() * DT.dayLength;
		if(repeating.getValue() && fq>0){
			if(action.has("frequency")){
				/*if ((int)(action.getDouble("frequency") / DT.dayLength) != frequency.getValue())*/ action.set("frequency", fq);
			} else action.set("frequency", fq);
		}
		else action.delete("frequency");
		
		if(action.has("candidate") && !Database.getItem(action.getString("candidate")).is("completed")){
			VerbObjectItem candidate = (VerbObjectItem) Database.getItem(action.getString("candidate"));
			candidate.setPrefaceText(verb);
			if(repeating.getValue() && fq>0 && candidate.getTime() - now > fq) candidate.setTime(now + fq);
			if(parent.getUID() != null && (!action.has("repeatParent") || !action.getString("repeatParent").equals(parent.getUID()))){ // TODO: if parent==null, remove it???
				action.set("repeatParent", parent.getUID());
				Item parentItem = Database.getItem(parent.getUID());
				if(!parentItem.children.contains(candidate.uid)) parentItem.add(candidate.uid);
			}
		} else { // TODO search for satisfactory candidate?
			VerbObjectItem candidate = VerbObjectItem.createVerbObjectItem(verb,item.uid);
			candidate.setTime(now + fq);
			action.set("candidate",candidate.uid);
			if(parent.getUID() != null) { // TODO: if parent==null, remove it???
				action.set("repeatParent", parent.getUID());
				Database.getItem(parent.getUID()).add(candidate.uid);
			}
		}

		// create or modify candidate if need be
	}

	public void setFocus() {
		verbBox.setFocus(true);
	}
}
