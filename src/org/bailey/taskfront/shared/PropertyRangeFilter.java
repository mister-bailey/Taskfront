package org.bailey.taskfront.shared;

public abstract class PropertyRangeFilter extends OrderInclusionFilter {
	//String filterProperty;
	double startOrder, endOrder;
	protected String key;
	@Override
	public double getStartOrder(){return startOrder;}
	@Override
	public double getEndOrder(){return endOrder;}
	@Override
	public void removeOrder(FilteredListItem parent,Item item){
		item.deleteProperty(key);
	}
	public PropertyRangeFilter(String key, double start, double end){
		this.key=key;
		startOrder = start;
		endOrder = end;
	}
	public void storeFilterData(Item item){
		item.setProperty("sortProperty", key); // ***** Redundant?? *****
		item.setProperty("startOrder", startOrder);
		item.setProperty("endOrder", endOrder);
	}
}

