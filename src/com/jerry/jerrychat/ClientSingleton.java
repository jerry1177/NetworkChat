package com.jerry.jerrychat;

import java.net.InetAddress;

public class ClientSingleton {

	private Client client;
	private static ClientSingleton instance = null;
	private ClientSingleton() {
		
	}
	
	public static synchronized ClientSingleton getInstance() {
		if (instance == null) 
			instance = new ClientSingleton();
		return instance;
		
	}
	public Client getClient() {
		return client;
	}
	public void setClient(String name, InetAddress address, int port) {
		client = new Client(name, address, port);
	}
}
