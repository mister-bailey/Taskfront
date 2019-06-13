package org.bailey.taskfront.shared;

import org.bailey.taskfront.client.Database;

public abstract class OrderSorter implements Sorter {
	public abstract double childOrder(Item parent,Item candidate);
	public abstract void setChildOrder(Item parent,Item candidate, double order);
	
	// tells us where child should be inserted in our UIDlist
	// this implementation is fine unless you have a quicker way
	@Override
	public int targetIndex(Item parent,double targetOrder){
		int index = parent.children.size();
		while(index>0 && targetOrder <= childOrder(parent,Database.getItem(parent.children.get(index-1)))) index--;
		return index;
	}
	
	// starts searching at startIndex.  list is assumed to be sorted, with the possible exception of startIndex.
	@Override
	public int targetIndex(Item parent,double targetOrder, int startIndex){
		int index = startIndex;
		int size = parent.children.size();
		while(index>0 && targetOrder <= childOrder(parent,Database.getItem(parent.children.get(index-1)))) index--;
		if(index==startIndex && index < size-1 && targetOrder > childOrder(parent,Database.getItem(parent.children.get(index+1)))){
			index++;
			while(index<size && targetOrder > childOrder(parent,Database.getItem(parent.children.get(index)))) index++;
		} 
		return index;
	}
	@Override
	public int targetIndex(Item parent,Item candidate){
		return targetIndex(parent,childOrder(parent,candidate));
	}
	@Override
	public int targetIndex(Item parent,Item candidate, int startIndex){
		return targetIndex(parent, childOrder(parent, candidate),startIndex);
	}
}

