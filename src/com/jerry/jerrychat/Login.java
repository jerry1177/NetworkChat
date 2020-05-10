package com.jerry.jerrychat;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Login extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textName;
	private JTextField textAddress;
	private JLabel addressLabel;
	private JLabel portLabel;
	private JTextField textPort;
	private boolean eWActive;
	private ErrorWindowListener eWListener;
	private ErrorWindow errorW;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Login frame = new Login();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Login() {
		eWActive = false;
		eWListener = new ErrorWindowListener() {
			public void onWindowExit() {
				eWActive = false;
			}
		};
		
		// make sure UI looks native
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		setTitle("Login");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 354, 381);
		setSize(300, 380);
		setResizable(false);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		textName = new JTextField();
		textName.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					loginAction();
				}
					
			}
		});
		textName.setBounds(85, 95, 130, 26);
		contentPane.add(textName);
		textName.setColumns(10);
		
		JLabel nameLabel = new JLabel("Name:");
		nameLabel.setBounds(85, 77, 45, 16);
		contentPane.add(nameLabel);
		
		textAddress = new JTextField();
		textAddress.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					loginAction();
				}
					
			}
		});
		
		textAddress.setText("localhost");
		textAddress.setToolTipText("e.g. 255.255.255.255");
		textAddress.setBounds(85, 154, 130, 26);
		contentPane.add(textAddress);
		textAddress.setColumns(10);
		
		addressLabel = new JLabel("IP Address:");
		addressLabel.setBounds(85, 133, 77, 16);
		contentPane.add(addressLabel);
		
		portLabel = new JLabel("Port:");
		portLabel.setBounds(85, 192, 37, 16);
		contentPane.add(portLabel);
		
		textPort = new JTextField();
		textPort.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) 
					loginAction();
				
			}
		});
		textPort.setText("3000");
		textPort.setToolTipText("e.g. 8192");
		textPort.setBounds(85, 213, 130, 26);
		contentPane.add(textPort);
		textPort.setColumns(10);
		
		JButton btnNewButton = new JButton("Login");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loginAction();
			}
		});
		btnNewButton.setBounds(111, 257, 77, 29);
		contentPane.add(btnNewButton);
	}
	
	private void loginAction() {
		int port = -1;
		InetAddress ip = null;
		if (textName.getText().isBlank() || textAddress.getText().isBlank() || textPort.getText().isBlank())
			displayError("Make sure all text boxes are filled!", eWListener);
		credentialsAndLogin(textName, port, ip);
	}
	
	
	private void credentialsAndLogin(JTextField textName, int port, InetAddress address) {
		
		
		try {
			address = InetAddress.getByName(textAddress.getText());
			port = Integer.parseInt(textPort.getText());
		} catch (UnknownHostException ex) {
			displayError("ip address is not valid!", eWListener);
			System.out.println("ip address is not valid!");
			//ex.printStackTrace();
			return;
		} catch (Exception ex) {
			displayError("make sure port is an integer and ip address is valid!", eWListener);
			System.out.println("make sure port is an integer and ip address is valid!");
			return;
		}
		
		if (port > 0) {
			Client client = new Client(textName.getText(), address, port);
			
			// set validation listener			
			client.registerRequestListener(new OnMessageListener() {
				@Override
				public void onClientValidate(String message) {
					String[] nameAndKey = message.split(",");
					if (nameAndKey.length == 2) {
						client.secureConnection(nameAndKey[1]);
						String valid = client.decryptString(nameAndKey[0]);
						if (valid.equals("valid")) {	
							System.out.println("Logged In!");
							if (eWActive)
								errorW.dispose();
							dispose();
							new ClientWindow(client);
						} 
					} else if (message.equals("invalidName")) {
						displayError("Invalid username", eWListener);
						System.out.println("Invalid username");
					} else {
						displayError("Server invalid response", eWListener);
						System.out.println("Server invalid response");
					}
				}
				@Override
				public void onCantConnectToServer(String error) {
					displayError(error, eWListener);
				}
				
				@Override
				public void onConnectionLost(String error) {
					displayError(error, eWListener);
				}
			});
			// connect client to server
			client.openConnection();
			// set client's listening thread
			client.receive();
			// send the client's name
			client.sendNameAndPublicKey(client.getName());
		}
		else {
			displayError("Port must be greater than 0", eWListener);
			System.out.println("Port must be greater than 0");
		}
	}
	
	private void displayError(String error, ErrorWindowListener listener ) {
		if (!eWActive) {
			eWActive = true;
			errorW = new ErrorWindow(error, eWListener);
			errorW.setAlwaysOnTop(true);
		} else {
			System.out.println("error window active");
		}
	}
	
}
