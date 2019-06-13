package org.bailey.taskfront.client;

import org.bailey.taskfront.shared.DT;
import org.bailey.taskfront.shared.Item;
import org.bailey.taskfront.shared.NeededActionListItem;

import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class NeededActionListEditPanel extends EditPanel {
	
	ItemSelectBox filter = new ItemSelectBox();
	TextBox verbBox = new TextBox();
	DoubleBox minFrequency = new DoubleBox();
	DoubleBox maxFrequency = new DoubleBox();
	ItemSelectBox parent = new ItemSelectBox();
	
	public NeededActionListEditPanel(String itemID){
		super(itemID);
		this.setStyleName("subEditPanel");
		this.add(new Label("From "));
		this.add(filter);
		this.add(new HTML(",<br>we must "));
		this.add(verbBox);
		this.add(new HTML(" items in this list<br>every "));
		this.add(minFrequency);
		this.add(new HTML(" to "));
		this.add(maxFrequency);
		this.add(new HTML(" days.<br>(Tasks appear in "));
		this.add(parent);
		this.add(new HTML(".)"));
	}
	
	public void update(){
		NeededActionListItem item = (NeededActionListItem) Database.getItem(itemID);
		filter.setUID(item.getFilter());
		verbBox.setText(item.getVerb());
		minFrequency.setValue((item.getMinFrequency() / DT.dayLength));
		maxFrequency.setValue((item.getMaxFrequency() / DT.dayLength));
		parent.setUID(item.getActionParent());
	}
	
	public void save(){
		NeededActionListItem item = (NeededActionListItem) Database.getItem(itemID);
		String verb = verbBox.getValue();
		if(verb != null && !verb.trim().equals("")) item.content.set("verb",verb);
		/*if((int)(item.getMinFrequency() / DT.dayLength) != minFrequency.getValue())*/ item.content.set("minFrequency",minFrequency.getValue() * DT.dayLength);
		/*if((int)(item.getMaxFrequency() / DT.dayLength) != maxFrequency.getValue())*/ item.content.set("maxFrequency",maxFrequency.getValue() * DT.dayLength);
		if(parent.getUID() != null) item.content.set("actionParent",parent.getUID());
		String filterID = filter.getUID()==null ? item.getFilter() : filter.getUID();
		if(filterID != null) item.replaceFilter(filterID);
	}

	public void setFocus() {}
	
}
