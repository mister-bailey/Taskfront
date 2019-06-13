package org.bailey.taskfront.shared;

import java.io.Serializable;

public class DatabaseEvent implements Serializable {
	public String uid;
	public DatabaseEvent(String u){
		uid=u;
	}
	public DatabaseEvent(){}
}

