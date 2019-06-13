package org.bailey.taskfront.client;


import org.bailey.taskfront.client.ItemPropertyEditPanel.PropertyType;
import org.bailey.taskfront.shared.UID;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.core.client.JsArrayString;

// Some obvious extensions to JavaScriptObject
public class JsObject extends JavaScriptObject {
	protected JsObject() { }
	
	public static native final JsObject newJsObject() /*-{
		return new Object();
	}-*/;
	public static native final JsObject newJsObjectFromNull() /*-{
		return Object.create(null);
	}-*/;
	
	// typed getters
	// Will explode and kill you if you ask for the wrong type (fix this later)
	public native final JsObject getJsObject(String key) /*-{
		return this[key];
	}-*/;
	public native final String getString(String key) /*-{
		return this[key];
	}-*/;
	public native final int getInt(String key) /*-{
		return Number(this[key]);
	}-*/;
	public native final double getDouble(String key) /*-{
		return Number(this[key]);
	}-*/;
	public native final boolean getBoolean(String key) /*-{
		return Boolean(this[key]);
	}-*/;
	
	public native final JsArrayString getJsArrayString(String key) /*-{
		return this[key];
	}-*/;
	public native final JsArrayInteger getJsArrayInteger(String key) /*-{
	return this[key];
}-*/;
	public native final JsArrayNumber getJsArrayNumber(String key) /*-{
	return this[key];
}-*/;
	
	public final String[] getStringArray(String key){
		return JsArrays.toArray(getJsArrayString(key));
	}
	public final int[] getIntArray(String key){
		return JsArrays.toArray(getJsArrayInteger(key));
	}
	public final double[] getDoubleArray(String key){
		return JsArrays.toArray(getJsArrayNumber(key));
	}

	// watch out for integer=0
	public native final boolean has(String key) /*-{
		return key in this;//!(!(this[key]));
	}-*/;
	
	// could throw exception if a property is not JsObject when expected!
	public final boolean hasSubProperty(String [] keys) {
		Object o=this;
		for(String key : keys){
			if(!((JsObject)o).has(key)) return false;
			o = ((JsObject)o).getJsObject(key);
		}
		return true;
	};
	public final void setSubProperty(String [] keys, JsObject value){
		JsObject o=this;
		for(int i=0; i<keys.length-1; i++){
			if(!o.has(keys[i])) o.set(keys[i],JsObject.newJsObject());
			o = o.getJsObject(keys[i]);
		}
		o.set(keys[keys.length-1],value);
	}
	public final void deleteSubProperty(String [] keys){
		JsObject o=this;
		for(int i=0; i<keys.length-1; i++){
			if(!o.has(keys[i])) return;
			o = o.getJsObject(keys[i]);
		}
		o.delete(keys[keys.length-1]);
	}
	
	// typed setters
	public native final void set(String key, String value) /*-{
		this[key]=value;
	}-*/;
	public native final void set(String key, double value) /*-{
		this[key]=value;
	}-*/;
	public native final void set(String key, int value) /*-{
		this[key]=value;
	}-*/;
	public native final void set(String key, boolean value) /*-{
	this[key]=value;
	}-*/;
	
	public native final void set(String key, JsObject value) /*-{
	this[key]=value;
	}-*/;	
	
	public native final void set(String key, JsArrayString value) /*-{
		this[key]=value;
	}-*/;
	public native final void set(String key, JsArrayInteger value) /*-{
		this[key]=value;
	}-*/;
	public native final void set(String key, JsArrayNumber value) /*-{
		this[key]=value;
	}-*/;
	
/*	public final void set(String key, Object[] value) {
		set(key, JsArrays.fromArray(value));
	}*/
	public final void set(String key, String[] value) {
		set(key, JsArrays.fromArray(value));
	}
	public final void set(String key, int[] value) {
		set(key, JsArrays.fromArray(value));
	}
	public final void set(String key, double[] value) {
		set(key, JsArrays.fromArray(value));
	}
	public native final void delete(String key) /*-{
		delete this[key];
	}-*/;
	
	public native final boolean isEmpty() /*-{
		for(var p in this){
			if(this.hasOwnProperty(p)) return false;
		}
		return true;
	}-*/;
	public native final JsArrayString getKeys() /*-{
		return Object.keys(this);
	}-*/;
	

	// JSON stuff
	public static native final JsObject eval(String s) /*-{
		return JSON.parse(s);
	}-*/;
	public native final String JSON()	/*-{
		return JSON.stringify(this);
	}-*/;
	
	public final JsObject clone(){
		return eval(JSON());
	}
	
	public native final boolean isString(String key) /*-{
		return (typeof this[key]) == 'string';
	}-*/;
	public native final boolean isNumber(String key) /*-{
		return (typeof this[key]) == 'number';
	}-*/;
	public native final boolean isBoolean(String key) /*-{
		return (typeof this[key]) == 'boolean';
	}-*/;
	public native final boolean isObject(String key) /*-{
		return (typeof this[key]) == 'object';
	}-*/;
	public native final boolean isNull(String key) /*-{
		return (this[key] == null);
	}-*/;
	
	public final PropertyType getType(String key){
		if(isNull(key)) return PropertyType.UNKNOWN;
		else if(isString(key)){
			if(UID.hasUID(getString(key))) return PropertyType.ITEM;
			else return PropertyType.STRING;
		} else if(isNumber(key)){
			double value = getDouble(key);
			if(value > 315569259747.) return PropertyType.DATE_TIME; // greater than a decade
			else if(value > 4.32e7) return PropertyType.DAYS; // greater than half a day
			else return PropertyType.NUMBER;
		} else if(isBoolean(key)) return PropertyType.BOOLEAN;
		else if(isObject(key)) return PropertyType.JS_OBJECT;
		else return PropertyType.UNKNOWN;
	}

}
