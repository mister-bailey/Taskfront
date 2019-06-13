package org.bailey.taskfront.shared;

public class PropertySorter extends OrderSorter {
	protected String key;
	public PropertySorter(String key) {this.key = key;}
	@Override
	public double childOrder(Item parent,Item child) {
		if(!child.hasProperty(key))return Double.MAX_VALUE;
		else return child.getDoubleProperty(key);
	}
	@Override
	public void setChildOrder(Item parent,Item child, double order) {
		child.setProperty(key, order);
	}
	@Override
	public void storeSorterData(Item item) {
		item.setProperty("sortProperty",key);
	}
}
