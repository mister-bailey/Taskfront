package org.bailey.taskfront.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.bailey.taskfront.shared.DatabaseEvent;
import org.bailey.taskfront.shared.ItemsWithTime;
import org.bailey.taskfront.shared.LoginInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>RemoteStoreService</code>.
 */
public interface RemoteStoreServiceAsync {
	public void getLoginInfo(String destination, AsyncCallback<LoginInfo> callback);
	public void save(Map<String,String> saveStrings, double callTime, AsyncCallback<Double> callback);
	public void load(Collection<String> uids, AsyncCallback<ItemsWithTime> callback);
	public void loadLaterThan(double time, AsyncCallback<ItemsWithTime> callback);
	public void applyDatabaseEvents(ArrayList<DatabaseEvent> events, double time,AsyncCallback<ItemsWithTime> callback);
	public void clear(AsyncCallback<Void> callback);
}
