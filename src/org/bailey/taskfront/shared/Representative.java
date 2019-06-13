package org.bailey.taskfront.shared;

import org.bailey.taskfront.client.JsObject;

public interface Representative {
	//public CandidateObserver candidateObserver();
	public void update(); // Doesn't update list!!
	public void updateProperty(String key,String value);
	public void updateProperty(String key,double value);
	public void updateProperty(String key,boolean value);
	public void updateSubProperty(String [] keys, JsObject value);
	public void updateDeleteProperty(String key);
	public void deleteRepresentative(); // Used when the objectItem is being deleted
	
	// Child candidate updates
	public void updateCandidate(Item parent, Item child);
	public void updateList();
	public void addCandidate(int index,String id);
	public void moveCandidate(int indexTo, String id);
	public void moveCandidate(int indexTo, int indexFrom);
	public void removeCandidate(String id);
	public boolean removeCandidate(int index);
}