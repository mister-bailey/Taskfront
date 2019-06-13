package org.bailey.taskfront.shared;

public interface Sorter {
	public double childOrder(Item parent,Item candidate);
	public void setChildOrder(Item parent,Item candidate, double order);
	public int targetIndex(Item parent,double targetOrder);
	public int targetIndex(Item parent,double targetOrder, int startIndex);
	public int targetIndex(Item parent,Item candidate);
	public int targetIndex(Item parent,Item candidate, int startIndex);	
	public void storeSorterData(Item item);
}
