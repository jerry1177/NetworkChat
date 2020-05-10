package com.jerry.jerrychat.server;

import java.net.InetAddress;
import java.net.Socket;
import javax.crypto.SecretKey;

public class ServerClient {
	private String name;
	private SecretKey secKey;
	private Socket socket;
	private InetAddress address;
	private int port;
	private final int ID;
	private static volatile int IDs = 0;
	
	/**
	 * Constructor that takes in the client's 
	 * name, the client's connection socket,
	 * and the symmetric key used to encrypt
	 * and decrypt all client and server 
	 * communication.
	 * @param name Clients name
	 * @param socket Client's socket
	 * @param secKey Symmetric client's key
	 */
	public ServerClient(String name, Socket socket, SecretKey secKey) {		
		this.name = name;
		this.socket = socket;
		this.secKey = secKey;
		this.address = socket.getInetAddress();
		this.port = socket.getPort();
		this.ID = IDs++;
	}
	
	/**
	 * This method returns the clients name.
	 * @return Client's name
	 */
	String getName() { return name; }
	/**
	 * This method returns the Client's 
	 * symmetric key
	 * @return Symmetric key
	 */
	SecretKey getSecretKey() { return secKey; }
	/**
	 * This method returns the client's IP
	 * address in the form of an InetAddress
	 * @return Client's IP address
	 */
	InetAddress getAddress() { return address; }
	
	/**
	 * This method returns the client's port number 
	 * in which the socket is connected to.
	 * @return The Client's port number 
	 */
	int getPort() { return port; }
	
	/**
	 * This method returns the socket in which
	 * the client is connected to.
	 * @return the client's socket connection
	 */
	public Socket getSocket() { return socket; }
	
	/**
	 * This is the class's destructor.
	 */
	public void finalize() throws Throwable {
		IDs--;
	}
	
	/**
	 * This method returns the client's ID
	 * that is assigned to it when it get's 
	 * created.
	 * @return Client's id number
	 */
	public int getID() { return ID; }
}
