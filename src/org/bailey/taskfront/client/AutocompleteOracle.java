package org.bailey.taskfront.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.SuggestOracle;

// stores a static database indexing ngrams to items, and furnishes this for autocomplete purposes.
public class AutocompleteOracle extends SuggestOracle implements Scheduler.ScheduledCommand {
	public AutocompleteOracle(){}
	
	private static SuggestOracle.Request request;
	private static SuggestOracle.Callback callback;
	
	// TODO: what if two items have the same text?
	// Currently, a newer modification will overwrite an older one.
	// If one of the items has its text changed, the old text now refers to nothing.
	private static TreeMap<String,String> map = new TreeMap<String,String>();
	public static void setText(String text, String uid){
		if(text==null || text.length()==0)return;
		map.put(text.toLowerCase(),uid);
	}
	public static void changeText(String oldText, String newText, String uid){
		map.remove(oldText.toLowerCase());
		if(newText.length()==0)return;
		map.put(newText.toLowerCase(),uid);
	}
	public static void changeText(String oldText, String newText){
		changeText(oldText,newText,map.get(oldText.toLowerCase()));
	}
	public static void removeText(String text) {map.remove(text.toLowerCase());}
	public static void clearMap(){map = new TreeMap<String,String>();}
	
	public void requestSuggestions(Request request, Callback callback) {
		if(AutocompleteOracle.request!=null) return;
		AutocompleteOracle.request=request;
		AutocompleteOracle.callback=callback;
		Scheduler.get().scheduleDeferred(this);
	}

	public void execute() {
		String query = request.getQuery().toLowerCase();
		int limit = request.getLimit();
		Response response = new Response();	
		ArrayList<ItemSuggestion> suggestions = new ArrayList<ItemSuggestion>();
		
		Set<Entry<String,String>> tailEntries = map.tailMap(query).entrySet();
		for(Entry<String,String> entry : tailEntries){
			if(entry.getKey().startsWith(query)){
				if(suggestions.size()>=limit){
					response.setMoreSuggestions(true);
					break;
				} else suggestions.add(new ItemSuggestion(entry.getValue()));
			}else break;
		}
		response.setSuggestions(suggestions);		
	
		callback.onSuggestionsReady(request,response);
		request=null;
		callback=null;
	}
	
	public class ItemSuggestion implements Suggestion {
		String displayString;
		String uid;
		public ItemSuggestion(String uid, String displayString){
			this.uid=uid;
			this.displayString=displayString;
		}
		public ItemSuggestion(String uid){
			this(uid,Database.getItem(uid).toString());
		}
		public String getDisplayString() {return displayString;}
		public String getReplacementString() {return "suggestion error";}
		public String getUID() {return uid;}
	}
}
