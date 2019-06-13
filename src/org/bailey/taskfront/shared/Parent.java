package org.bailey.taskfront.shared;

public interface Parent {
	public void updateChild(Item item);

	public void removeChild(String uid); // ????? ******** ??????????
	public void removeChild(int index);
}
