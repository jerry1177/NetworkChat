package com.jerry.jerrychat.server;


public class ServerMain {
	public ServerMain(int port) {
		new Server(port);
	}
	
	public static void main(String[] args) {
		
		// make sure user enters a port
		if (args.length != 1) {
			System.out.println("Usage: java -jar JerryChatServer.jar [port]");
			return;
		}
		int port;
		try {
			port = Integer.parseInt(args[0]);
		} catch (Exception e) {
			System.out.println("The port number must be an integer!");
			return;
		}
		if (port < 0 || port > 65535) {
			System.out.println("Port number must be between 0 and 65535!");
		}

		new ServerMain(port);
	}
}
