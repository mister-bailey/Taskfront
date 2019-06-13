package org.bailey.taskfront.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LoginInfo implements IsSerializable {
	private static final long serialVersionUID = -1979044798842479243L;
	public String userId;
	public String loginUrl;
	public String logoutUrl;
	public LoginInfo(){}
	public LoginInfo(String userId, String loginUrl, String logoutUrl){
		this.userId=userId;
		this.loginUrl=loginUrl;
		this.logoutUrl=logoutUrl;
	}
	public LoginInfo(String loginUrl, String logoutUrl){
		this.loginUrl=loginUrl;
		this.logoutUrl=logoutUrl;
	}
}
