package org.bailey.taskfront.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bailey.taskfront.shared.Item;
import org.bailey.taskfront.shared.VerbObjectItem;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

public class ItemEditPanel extends EditPanel {
	
	TextBoxBase text;
	ItemSelectBox object;
	ListBox typeBox;
	CheckBox completed;
	//Label completedLabel;
	DateBox time;
	VerticalPanel actionPanel = new VerticalPanel();
	EditPanel typeEditPanel;
	VerticalPanel otherPanel = new VerticalPanel();
	
	public static ItemEditPanel createPanel(String itemID){
		return new ItemEditPanel(itemID);
	}
	
	public ItemEditPanel(String itemID){
		super(itemID);
		
		add(text=new AutosizeTextArea());
		add(typeBox=new ListBox());
		typeBox.addItem("Item");
		typeBox.addItem("Verb-object","VerbObjectItem");
		typeBox.addItem("Timespan filter","TimeSpanItem");
		typeBox.addItem("Boolean property filter","IsPropertyItem");
		typeBox.addItem("Has-property filter","HasPropertyItem");
		typeBox.addItem("Needed-action list","NeededActionListItem");
		// confirm type change!
		// TODO typeBox.addValueChangeHandler(...);
		add(new HTML("<br>"));
		
		add(new Label("Date"));
		add(time=new DateBox());
		add(completed=new CheckBox());
		add(new HTML("<br>"));

		String type=Database.getItem(itemID).getType();
		if(type.equals("NeededActionListItem")){
			add(typeEditPanel = new NeededActionListEditPanel(itemID));
		} else if (type.equals("TimeSpanItem")){
			//add(new TimeSpanEditPanel());
		} else if (type.equals("IsPropertyItem")){
			//add(new IsPropertyEditPanel());
		} else if (type.equals("HasPropertyItem")){
			//add(new HasPropertyEditPanel());
		} else if (type.equals("VerbObjectItem")){
			//add(new VerbObjectEditPanel());
			insert(object = new ItemSelectBox(),1);
		};
		
		add(actionPanel);
		actionPanel.setStyleName("subEditPanel");
		add(otherPanel);
		otherPanel.setStyleName("subEditPanel");
		
		update();		
	}
	
	public void save(){
		// TODO: Deal with type changes!
		
		Item item = Database.getItem(itemID);
		boolean selfSave = item.selfSave;
//		item.selfSave=false;
		
		if(typeEditPanel != null) typeEditPanel.save();
		
		item.setText(text.getText());
		
		Date date = time.getValue();
		if(date != null) item.setTime(date.getTime());
		else item.deleteProperty("time");
		
		item.complete(completed.getValue());
		
		for(Widget w : actionPanel){
			if(w instanceof NeededActionEditPanel) ((NeededActionEditPanel)w).save();
		}
		
		for(Widget w : otherPanel){
			if(w instanceof ItemPropertyEditPanel) ((ItemPropertyEditPanel)w).save();
		}
		
		item.updateObservers();
		if(selfSave){
			item.saveContent();
			item.selfSave=true;
		}
	}
	
	public void update(){
		Item item = Database.getItem(itemID);
		
		String type=item.getType();
		if(type==null || type.equals("") || type.equals("Item")) typeBox.setSelectedIndex(0);
		else if(type.equals("VerbObjectItem")) typeBox.setSelectedIndex(1);
		else if(type.equals("TimeSpanItem")) typeBox.setSelectedIndex(2);
		else if(type.equals("IsPropertyItem")) typeBox.setSelectedIndex(3);
		else if(type.equals("HasPropertyItem")) typeBox.setSelectedIndex(4);
		else if(type.equals("NeededActionListItem")) typeBox.setSelectedIndex(5);

		if(type.equals("VerbObjectItem")) {
			text.setText(((VerbObjectItem) item).getPrefaceText());
			object.setValue(item.content.getString("filterID"));
		}
		else text.setText(item.getText());

		
		if(item.hasProperty("time")) time.setValue(new Date((long)item.getTime()),false);
		else time.setValue(null,false);
		
		completed.setValue(item.is("completed"));
		if(item.is("completed") && item.hasProperty("completionTime"))
			completed.setText("Completed on " +
				DateTimeFormat.getFormat("dd/MM/yyyy").format(new Date((long)item.getDoubleProperty("completionTime"))));
		else completed.setText("Completed");
		
		actionPanel.clear();
		actionPanel.add(new Label("Needed actions"));
		if(item.hasProperty("neededActions")){
			for(String verb : JsArrays.toArray(item.content.getJsObject("neededActions").getKeys())){
				actionPanel.add(new NeededActionEditPanel(itemID, verb));
			}
		}
		actionPanel.add(new Button("New action", new ClickHandler(){
			public void onClick(ClickEvent event) {
				NeededActionEditPanel newPanel = new NeededActionEditPanel(ItemEditPanel.this.itemID,"");
				ItemEditPanel.this.actionPanel.insert(newPanel,ItemEditPanel.this.actionPanel.getWidgetCount()-1);
				newPanel.setFocus();
			}			
		}));
		
		if(typeEditPanel != null)typeEditPanel.update();
		
		otherPanel.clear();
		otherPanel.add(new Label("Other properties"));
		String [] otherProperties = JsArrays.toArray(item.content.getKeys());
		//otherProperties.removeAll(ItemPropertyEditPanel.excludedProperties);
		for(String key : otherProperties){
			if(!ItemPropertyEditPanel.excludedProperties.contains(key)) otherPanel.add(new ItemPropertyEditPanel(itemID, key));
		}
		otherPanel.add(new Button("New Property", new ClickHandler(){
			public void onClick(ClickEvent event) {
				ItemPropertyEditPanel newPanel = new ItemPropertyEditPanel(ItemEditPanel.this.itemID,"");
				ItemEditPanel.this.otherPanel.insert(newPanel,ItemEditPanel.this.otherPanel.getWidgetCount()-1);
				newPanel.setFocus();
			}			
		}));
	}

	public void setFocus() {
		text.setFocus(true);
	}
	
	//protected ItemEditPanel(){super(itemID);}
	
}
