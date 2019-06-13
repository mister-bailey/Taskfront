package org.bailey.taskfront.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import org.bailey.taskfront.shared.DT;
import org.bailey.taskfront.shared.Item;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
//import org.bailey.taskfront.client.ItemPropertyEditPanel.PropertyType.*;

public class ItemPropertyEditPanel extends EditPanel {
	public enum PropertyType{
		BOOLEAN,
		STRING,
		NUMBER,
		INTEGER,
		DAYS,
		DATE_TIME,
		ITEM,
		JS_OBJECT,
		UNKNOWN
	}
	public static final PropertyType defaultType = PropertyType.NUMBER;
	public static HashMap<String,PropertyType> propertyTypes = new HashMap<String,PropertyType>();
	static {
		propertyTypes.put("repeatPeriod",PropertyType.DAYS);
		propertyTypes.put("repeatParent",PropertyType.ITEM);
		propertyTypes.put("defaultChildAction",PropertyType.STRING);
		propertyTypes.put("defaultView",PropertyType.STRING);
	}
	public static ArrayList<String> excludedProperties = new ArrayList<String>();
	static {
		excludedProperties.add("neededActions");
		excludedProperties.add("time");
		excludedProperties.add("completed");
		excludedProperties.add("type");
		excludedProperties.add("text");
		excludedProperties.add("collapsed");
	}
	static MultiWordSuggestOracle keyOracle = new MultiWordSuggestOracle();
	static { keyOracle.addAll(propertyTypes.keySet()); }
	
	public PropertyType type;
	public boolean deleted=false;
	public String key;
	public SuggestBox keyBox = new SuggestBox(keyOracle);
	public HorizontalPanel hp = new HorizontalPanel();
	public HasValue value;
	public ValueListBox<PropertyType> typeBox = new ValueListBox<PropertyType>(new AbstractRenderer<PropertyType>(){
		public String render(PropertyType type){return type==null ? "???" : type.toString();}
	});
	{typeBox.setAcceptableValues(Arrays.asList(PropertyType.values()));	}
	
	public ItemPropertyEditPanel(String itemID, String key){
		super(itemID);
		this.add(hp);
		
		this.setStyleName("subEditPanel");
		this.key = key;
		hp.add(keyBox);
		
		hp.add(new HTML(" = "));
		setType();
		if(this.type==null) setType(defaultType);
		
		hp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		
		hp.add(new HTML()); // spacer
		
		hp.add(typeBox);
		hp.add(new Button("X", new ClickHandler(){
			public void onClick(ClickEvent event) {
				ItemPropertyEditPanel.this.deleteProperty();
			}
		}));
		
		update();
		typeBox.addValueChangeHandler(new ValueChangeHandler<PropertyType>(){
			public void onValueChange(ValueChangeEvent<PropertyType> event) {
				ItemPropertyEditPanel.this.setType(event.getValue());
			}			
		});
		keyBox.getValueBox().addValueChangeHandler(new ValueChangeHandler<String>(){
			public void onValueChange(ValueChangeEvent<String> event) {
				if(propertyTypes.containsKey(event.getValue())){
					ItemPropertyEditPanel.this.setType(propertyTypes.get(event.getValue()));
					DOM.setElementPropertyBoolean(ItemPropertyEditPanel.this.typeBox.getElement(), "disabled", true);					
				}
			}			
		});
		keyBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>(){
			public void onSelection(SelectionEvent<Suggestion> event) {
				String newKey = event.getSelectedItem().getReplacementString();
				if(propertyTypes.containsKey(newKey)){
					ItemPropertyEditPanel.this.setType(propertyTypes.get(newKey));
					DOM.setElementPropertyBoolean(ItemPropertyEditPanel.this.typeBox.getElement(), "disabled", true);
					if(ItemPropertyEditPanel.this.value instanceof Focusable)
						((Focusable) ItemPropertyEditPanel.this.value).setFocus(true);
				}
			}
		});

	}
	protected void setType(){
		if(propertyTypes.containsKey(this.key)){
			setType(propertyTypes.get(this.key));
			DOM.setElementPropertyBoolean(typeBox.getElement(), "disabled", true);
		} else {
			Item item = Database.getItem(itemID);
			if(item.content.has(this.key)) setType(item.content.getType(this.key));
			DOM.setElementPropertyBoolean(typeBox.getElement(), "disabled", false);
			// else setType(defaultType);
		} 
	}

	protected void deleteProperty() {
		this.deleted=true;
		this.setVisible(false);
	}
	protected void setType(PropertyType type){
		if(this.type==type) return;
		typeBox.setValue(type,false);
		this.type=type;
		switch(type){
		case BOOLEAN:
			value = new CheckBox();
			break;
		case STRING:
			value = new TextBox();
			break;
		case NUMBER:
			value = new DoubleBox();
			break;
		case INTEGER:
			value = new IntegerBox();
			break;
		case DAYS:
			value = new DoubleBox();
			break;
		case DATE_TIME:
			value = new DateBox();
			break;
		case ITEM:
			value = new ItemSelectBox();
			break;
		// JS_OBJECT ???
		}
		if(hp.getWidgetCount() > 2) hp.removeCandidate(2);
		hp.insert((Widget)value,2);
	}

	public void update() {
		if(deleted)return;
		Item item = Database.getItem(itemID);
		if(!item.hasProperty(key)){
			this.removeFromParent();
			return;
		}
		keyBox.setText(key);
		switch(type){
		case BOOLEAN:
			((HasValue<Boolean>)value).setValue(item.content.getBoolean(key));
		case STRING:
			((HasValue<String>)value).setValue(item.content.getString(key));
			break;
		case NUMBER:
			((HasValue<Double>)value).setValue(item.content.getDouble(key));
			break;
		case INTEGER:
			((HasValue<Integer>)value).setValue((int) item.content.getDouble(key));
			break;
		case DAYS:
			((HasValue<Double>)value).setValue(item.content.getDouble(key) / DT.dayLength);
			break;
		case DATE_TIME:
			((HasValue<Date>)value).setValue(new Date((long) item.content.getDouble(key)));
			break;
		case ITEM:
			((HasValue<String>)value).setValue(item.content.getString(key));
			break;
		}
	}

	// only stores to the object---doesn't save to database!
	public void save() {
		Item item = Database.getItem(itemID);
		
		String keyBoxText = keyBox.getText().trim();
		if(key==null || key.equals("")) key = keyBoxText;
		if(deleted || keyBoxText.equals("")){
			item.content.delete(keyBoxText);
			return;
		}
		if(!key.equals(keyBoxText)){
			item.content.delete(key);
			key=keyBoxText;
		}
		
		switch(this.type){
		case BOOLEAN:
			item.content.set(this.key,((HasValue<Boolean>)value).getValue());
		case STRING:
			item.content.set(this.key,((HasValue<String>)value).getValue());
			break;
		case NUMBER:
			item.content.set(this.key,((HasValue<Double>)value).getValue());
			break;
		case INTEGER:
			item.content.set(this.key,((HasValue<Integer>)value).getValue());
			break;
		case DAYS:
			item.content.set(this.key,DT.dayLength * ((HasValue<Double>)value).getValue());
			break;
		case DATE_TIME:
			item.content.set(this.key,((HasValue<Date>)value).getValue().getTime());
			break;
		case ITEM:
			item.content.set(this.key,((HasValue<String>)value).getValue());
			break;
		}
	}

	public void setFocus() {
		keyBox.setFocus(true);
	}

}
