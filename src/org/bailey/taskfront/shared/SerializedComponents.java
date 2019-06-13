package org.bailey.taskfront.shared;

import java.io.Serializable;
import java.util.ArrayList;

import com.google.appengine.api.datastore.Entity;

public class SerializedComponents implements Serializable{
	public String uid;
	public String contentString;
	public String contentCacheString;
	public String type; 
	public ArrayList<String> list;  // Used by standard items to store children
	public SerializedComponents(String u, String type, String contentString, String contentCacheString, ArrayList<String> l){
		uid=u;
		this.type = type;
		this.contentString=contentString;
		this.contentCacheString=contentString;
		list=l;
	}
	public SerializedComponents(String u, String type, String contentString, ArrayList<String> l){
		uid=u;
		this.type = type;
		this.contentString=contentString;
		this.contentCacheString=null;
		list=l;
	}
	public SerializedComponents(Entity entity){
		uid = entity.getKey().getName();
		type = (String)entity.getProperty("type");
		contentString = (String)entity.getProperty("content");
		if(entity.hasProperty("contentCache")) contentCacheString = (String)entity.getProperty("contentCache");
		else contentCacheString = null;
		list = (ArrayList<String>)entity.getProperty("list");
	}
	private SerializedComponents(){}
}

