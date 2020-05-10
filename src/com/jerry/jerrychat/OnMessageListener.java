package com.jerry.jerrychat;

public abstract class OnMessageListener {
	public String onBeforeMessageSent(String message) {
		return message;
	}
	public void onMessageRecieved(String message) {
		
	}
	public void onClientValidate(String message) {
		
	}
	
	public void onCantConnectToServer(String error) {
		
	}
	
	public void onConnectionLost(String error) {
		
	}
}
