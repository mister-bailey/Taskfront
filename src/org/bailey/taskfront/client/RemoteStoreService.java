package org.bailey.taskfront.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.bailey.taskfront.shared.DatabaseEvent;
import org.bailey.taskfront.shared.ItemsWithTime;
import org.bailey.taskfront.shared.LoginInfo;
import org.bailey.taskfront.shared.NotLoggedIn;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("store")
public interface RemoteStoreService extends RemoteService {
	
	// Stores the key/value pairs, returns the time of the original call
	double save(Map<String,String> saveStrings, double callTime) throws NotLoggedIn;
	
	public ItemsWithTime load(Collection<String> uids) throws NotLoggedIn;
	
	public ItemsWithTime loadLaterThan(double time) throws NotLoggedIn;
	
	public ItemsWithTime applyDatabaseEvents(ArrayList<DatabaseEvent> events, double time) throws NotLoggedIn;
	
	public LoginInfo getLoginInfo(String destination);
	
	public Void clear() throws NotLoggedIn;
}
