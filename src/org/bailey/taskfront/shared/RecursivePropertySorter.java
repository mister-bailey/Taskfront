package org.bailey.taskfront.shared;

import org.bailey.taskfront.client.Database;
import org.bailey.taskfront.shared.RecursiveFilterItem.RecursiveSorter;

public class RecursivePropertySorter extends PropertySorter implements RecursiveSorter {
	// ******** The following method is inefficient!!! *********
	// Maybe cache "recursive order properties" somehow?
	@Override
	public double childOrder(Item parent,Item candidate){
		double minOrder = super.childOrder(parent,candidate);
		for(String ccID : candidate.children){
			minOrder = Double.min(minOrder,childOrder(candidate,Database.getItem(ccID)));
		}
		return minOrder;
	}
	
	public RecursivePropertySorter(String key){
		super(key);
	}
	
	public RecursiveSorter createRecursiveSorter(Item parent,Item objectItem){return this;}
}

