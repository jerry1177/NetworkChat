package com.jerry.jerrychat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;

public class Server implements Runnable {
	
	protected static volatile HashMap<String, ServerClient> clients = null;
	protected static volatile Queue<String> messageQueue = null;
	
	private ServerSocket sSocket;
	private Signature sign;
	private boolean isRunning = false;
	private Thread run, manage, send, receive;
	
	public Server( int port) {
		Server.clients = new HashMap<>();
		Server.messageQueue = new LinkedList<>();
		try {
			sSocket = new ServerSocket(port);
            System.out.println("Server started");
            sign = Signature.getInstance("SHA256withRSA");
            
		}  catch (Exception e) {
			e.printStackTrace();
			return;
		} 
		
		run = new Thread(this, "Server");
		run.start();
		//System.out.println("Hello");
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		isRunning = true;
		manageClients();
		receive();
	}
	
	/**
	 * This method spawns a new thread that 
	 * sends a message to all clients whenever a message
	 * is put into the queue
	 */
	private void manageClients() {
		manage = new Thread("manage") {
			public void run() {
				System.out.println("Client Manager ready...");
				while(isRunning) {
					if (!messageQueue.isEmpty()) { 		
						String message = messageQueue.remove();
						//System.out.println(message);
						sendToAll(message);						
					}
				}
			}
		};
		manage.start();
	}
	
	
	/**
	 * This method encrypts a symmetric key encrypted
	 * Base64 encoded string message. 
	 * @param message string message to be encrypted
	 * @param secKey is the symmetric key used to encrypt message
	 * @return encrypted Base64 encoded string message
	 */
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
	
	/**
	 * This method decrypts a symmetric key encrypted
	 * Base64 encoded string message. 
	 * @param message Base64 encoded string message
	 * @param secKey is the symmetric key used to decrypt message
	 * @return decrypted string message
	 */
	
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
	
	
	/**
	 * This meathod spawns a new thread that listens for 
	 * new socket connections. Once it has got a new connection
	 * it validates the client, sends them a new asymmetric key,
	 * and spawns a new thread to listen for any incoming messages from
	 * client.
	 */
	private void receive() {
		receive = new Thread("Receive") {
			public void run() {
				while(isRunning) {
		            System.out.println("Waiting for a client ..."); 
		            Socket socket;
		            ServerClient serverClient;
					try {
						socket = sSocket.accept();
						socket.setKeepAlive(true);
						// listen for client name
						DataInputStream in = new DataInputStream(socket.getInputStream());
						String m = in.readUTF();
						//System.out.println("message:\n" + m);
						String[] nameAndKey = m.split(",");
						if (nameAndKey.length != 3) {
							System.out.println("Invalid packet!");
							System.out.println("Size of packet array: " + nameAndKey.length);
							continue;
						}
												
						// extract the name and key from the validation request
						String name = nameAndKey[1];
						byte[] publicBytes = Base64.getDecoder().decode(nameAndKey[2]);
						X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
						KeyFactory keyFactory = KeyFactory.getInstance("RSA");
						PublicKey pubKey = keyFactory.generatePublic(keySpec);
						//System.out.println(pubKey);
						
						// extract signature
						byte[] sig = Base64.getDecoder().decode(nameAndKey[0]);
						//System.out.println("sig: " + sig.toString());
						sign.initVerify(pubKey);
						sign.update("realclient".getBytes());
						
						if (!sign.verify(sig)) {
							System.out.println("Warning: Signiture could not be identified!");
							send(socket, "Sig could not be verified");
							socket.close();
							continue;
						}
						
						// if the name is already in use
						if (clients.get(name) != null ) {
							System.out.println("Client name already taken: "+clients.get(name).getName());
							send(socket, "invalidName");
							continue;
						}
						
						// generate symmetric key
						KeyGenerator generator = KeyGenerator.getInstance("AES");
						generator.init(256);// The AES key size in number of bits
						SecretKey secKey = generator.generateKey();
						//System.out.println("Symetric key = " + secKey);
						
						// encrypte result text with symmetric key
						Cipher aesCipher = Cipher.getInstance("AES");
						aesCipher.init(Cipher.ENCRYPT_MODE, secKey);
						byte[] byteCipherText = aesCipher.doFinal("valid".getBytes());
						
						
						// encrypt public key
						Cipher pubCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
						pubCipher.init(Cipher.ENCRYPT_MODE, pubKey);
						byte[] encSKeyBytes = pubCipher.doFinal(secKey.getEncoded());
						
						// convert encrypted message and secret
						String validText = Base64.getEncoder().encodeToString(byteCipherText);
						String secK = Base64.getEncoder().encodeToString(encSKeyBytes);
						
						// send valid text and encoded secret key
						send(socket, validText+","+secK);
						// make a new client
						serverClient = new ServerClient(name, socket, secKey);
						clients.put(name, serverClient);
						// let chat room know that a new client connected
						messageQueue.add(name+" connected.");
						System.out.println("client " + name + " connected!");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}
					
					new Thread("Listen ("+clients.size()+")") {
						public void run() {
							DataInputStream dis;
							try {
								dis = new DataInputStream(socket.getInputStream());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								return;
							}
							while (!socket.isClosed()) {
								try {
									// get message
									String message = dis.readUTF();
									// decrypt message
									message = decryptString(message, serverClient.getSecretKey());
									//System.out.println("message recieved: " + message);
									// add message to message queue
									messageQueue.add(serverClient.getName() + ": " + message);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									//e.printStackTrace();
									// kick client from chat
									try {
										clients.get(serverClient.getName()).getSocket().close();
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
									// remove client from hash map
									clients.remove(serverClient.getName());
									// let chat group know that the client has been disconnected
									messageQueue.add(serverClient.getName()+" disconnected.");
									System.out.println("we have " + clients.size() + " now!");
									return;		
								}
							}
						}
					}.start();
				}
			}
		};
		receive.start();
	}
	
	/**
	 * This method sends the message to every client 
	 * in the client list
	 * @param message to be sent to every client
	 */
	private void sendToAll(String message) {
		System.out.println(message);
		clients.forEach((k, v) -> {
			String secureMessage = encryptString( message, v.getSecretKey());
			send(v.getSocket(), secureMessage);			
			});
	}
	
	/**
	 * This method spawns a thread to send a 
	 * message to one client.
	 * @param socket that the specific client is on
	 * @param message to be sent to specific client
	 */
	private void send(final Socket socket, final String message) {
		send = new Thread("Send") {
			public void run() {
				try {
					DataOutputStream out = new DataOutputStream(socket.getOutputStream());					
					out.writeUTF(message);
					if (message.equals("invalidName"))
						socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		send.start();
	}
}
