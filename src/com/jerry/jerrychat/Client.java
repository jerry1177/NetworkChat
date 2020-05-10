package com.jerry.jerrychat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


enum ClientError {
	CantConnect,
	ConnectionLost,
	NoRequestListener
}

public class Client {
	
	// handler
	private OnMessageListener requestListener = null;
	
	// security
	private volatile Signature sign;
	private KeyPairGenerator keyPairGen;
	private KeyPair pair;
	private volatile SecretKey secretKey;
	
	
	private String name;
	private int port; 		  // server's port
	private InetAddress ip;   // server's ip address
	private Socket socket; 
    private DataInputStream  input; 
    private DataOutputStream out;
    
	private Thread send, receive;
	private boolean isSecure;
	
	// set handler
	public void registerRequestListener(OnMessageListener listener) {
		this.requestListener = listener;
	}
	public void unregisterRequestListener() { requestListener = null; }
	
	/**
	 * This is the clients constructor. 
	 * When this is called the client automatically
	 * makes a connection with the server
	 * @param name The name of the client
	 * @param ipAddress The server's IP address
	 * @param port The Server's Port number
	 */
	public Client(String name, InetAddress ipAddress, int port) {

		this.name = name;
		this.ip = ipAddress;
		this.port = port;
		this.isSecure = false;
		//*
		try {
			sign = Signature.getInstance("SHA256withRSA");
			keyPairGen = KeyPairGenerator.getInstance("RSA");
			keyPairGen.initialize(2048);
			pair = keyPairGen.generateKeyPair();
			//System.out.println("Private key: " + pair.getPrivate());
			//System.out.println("Public key: " + pair.getPublic());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		
	}
	
	/**
	 * This method sends the server
	 * the client's name and the 
	 * public key for data security
	 * @param name The client's name
	 */
	public void sendNameAndPublicKey(String name) {
		String publicK = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
		String sig = "";
		try {
			sign = Signature.getInstance("SHA256withRSA");
			//Initializing the signature
		    sign.initSign(pair.getPrivate());
		    //Adding data to the signature
		    sign.update("realclient".getBytes());
		    //Calculating the signature
		    byte[] signature = sign.sign();
		    sig = Base64.getEncoder().encodeToString(signature);
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(sig +","+name+","+publicK);
		send(sig+","+name+","+publicK);
	}
	
	/**
	 * This methods turns on encryption
	 * NOTE: Make sure to turn on encryption after
	 * sending the clients name and public key to
	 * the server
	 * @param pubKey server's public key
	 * @return true if the connection is secure
	 */
	
	public boolean secureConnection(String secKey) { 
		
		// make sure public key works
		byte[] secBytes = decryptString(secKey,  pair.getPrivate());
		
		try {			
			this.secretKey = new SecretKeySpec(secBytes, 0, secBytes.length, "AES");
			//System.out.println("\nSymmetric key: "+ secretKey);
			//System.out.println(this.secretKey);
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, this.secretKey);
			//System.out.println("Server secret key: " + this.secretKey);
			isSecure = true; 
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			isSecure = false; 
		}
		 return isSecure;
		}
	
	private byte[] decryptString(String message, PrivateKey privKey) {
		byte[] cipherText = message.getBytes();
		try {
			  
			  Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");		        
		      //Initializing a Cipher object
		      cipher.init(Cipher.DECRYPT_MODE, privKey);			  
		      //Adding data to the cipher	  
		      cipher.update(Base64.getDecoder().decode(message));			  
		      //encrypting the data
		      cipherText = cipher.doFinal();	
		      return cipherText;
		      
		} catch (NoSuchAlgorithmException | NoSuchPaddingException 
				| InvalidKeyException | IllegalBlockSizeException 
				| BadPaddingException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	         
		return cipherText;
	}
	
	private String encryptString(String message, SecretKey secKey) {
		try {
			 Cipher cipher = Cipher.getInstance("AES");		        
		      //Initializing a Cipher object
		      cipher.init(Cipher.ENCRYPT_MODE, secKey);			  
		      //encrypting the data
		      byte[] cipherText = cipher.doFinal(message.getBytes());	
		      return Base64.getEncoder().encodeToString(cipherText);
		      
		      
		} catch (NoSuchAlgorithmException | NoSuchPaddingException 
				| InvalidKeyException | IllegalBlockSizeException 
				| BadPaddingException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	         
		return message;
	}
	
	private String decryptString(String message, SecretKey secKey) {
		try {
			  
			  Cipher cipher = Cipher.getInstance("AES");		        
		      //Initializing a Cipher object
		      cipher.init(Cipher.DECRYPT_MODE, secKey);			  
		      //encrypting the data
		      byte[] cipherText = cipher.doFinal(Base64.getDecoder().decode(message));	
		      return new String(cipherText, "UTF8");
		      
		} catch (NoSuchAlgorithmException | NoSuchPaddingException 
				| InvalidKeyException | IllegalBlockSizeException 
				| BadPaddingException | UnsupportedEncodingException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	         
		return message;
	}
	
	public String decryptString(String message) {
		try {
			  
			  Cipher cipher = Cipher.getInstance("AES");		        
		      //Initializing a Cipher object
		      cipher.init(Cipher.DECRYPT_MODE, this.secretKey);			  
		      //Adding data to the cipher	  
		      cipher.update(Base64.getDecoder().decode(message));			  
		      //encrypting the data
		      byte[] cipherText = cipher.doFinal();	
		      return new String(cipherText, "UTF8");
		      
		} catch (NoSuchAlgorithmException | NoSuchPaddingException 
				| InvalidKeyException | IllegalBlockSizeException 
				| BadPaddingException | UnsupportedEncodingException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	         
		return message;
	}
	
	/**
	 * This method returns the name of the client
	 * @return the Client's name
	 */
	public String getName() { return name; }
	
	/**
	 * This method gets the server's ip address
	 * that the client is connected to
	 * @return Server's ip address
	 */
	public InetAddress getAddress() {
		return ip;
	}
	
	/**
	 * This method returns the server's port 
	 * that the client is connected to
	 * @return Server's port
	 */
	public int getPort() { return port; }
	
	
	/**
	 * 
	 * @param address is the server's IP address
	 * @param port is the server's Port
	 * @return true if initial connection is made between the client and the server
	 */
	public boolean openConnection() {
		try {
			socket = new Socket(ip, port);
			input  = new DataInputStream(socket.getInputStream());
			out    = new DataOutputStream(socket.getOutputStream()); 
		}  catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			if (requestListener != null)
				requestListener.onCantConnectToServer(getErrorMsg(ClientError.CantConnect));
			System.out.println("Could not connect to server");
			return false;
		}
		return true;
	}
	
	/**
	 * This function tells you whether yu are connected to the server or not
	 * @return true if you are connected to server
	 */
	public boolean hasConnection() {
		return !socket.isClosed();
	}
	
	/**
	 * This method makes a new thread that listens
	 * for incoming calls from the server
	 * Note: There should only be one thread listening
	 * 		 per client.
	 */
	public void receive() {	
		receive = new Thread("receive") {
			public void run() {
				System.out.println("Receive is running");
				try {
				if (socket != null)
				while (!socket.isClosed()) {
					try {
						String message = input.readUTF();
						//System.out.println(message);
						if (requestListener != null) {
							if (isSecure) {
								// decrypt message
								message = decryptString(message, secretKey);						
							}
							requestListener.onMessageRecieved(message);
							requestListener.onClientValidate(message);
						} else {
							if (requestListener != null)
								requestListener.onCantConnectToServer(getErrorMsg(ClientError.NoRequestListener));
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.out.println("Connection lost");						
						if (requestListener != null)
							requestListener.onConnectionLost(getErrorMsg(ClientError.ConnectionLost));
						socket.close();
					}
				}
				} catch (Exception e) {
					e.printStackTrace();
					//if (requestListener != null)
						requestListener.onCantConnectToServer(getErrorMsg(ClientError.CantConnect));
					System.out.println("Couldn't connect to server");
					
				}
			}
		};
		receive.start();
	}
	
	
	public  void send(final String data) {
		send = new Thread("send") {
			public void run() {
				try {
					String message = data;
					// let the client user do any last minute changes to message
					if (requestListener != null) // callback
						message = requestListener.onBeforeMessageSent(message);
					
					if (isSecure) {
						message = encryptString(message, secretKey);
						//System.out.println("secure message: "+ message);
					}
					out.writeUTF(message);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					
					if (requestListener != null)
						requestListener.onCantConnectToServer(getErrorMsg(ClientError.CantConnect));
					System.out.println("Could not connect to server");
				}
			}
		};
		send.start();
	}
	
	
	private String getErrorMsg(ClientError error) {
		switch (error) {
		case CantConnect:
			return "Could not connect to server";
		case ConnectionLost:
			return "Connection lost";
		case NoRequestListener:
			return "Server sent message, but message could not be handle\n"
					+ "because you must implement a request listener!";
		default:
			return "error does not exist!";
		}
	}
	
	
	
}
