package com.jerry.jerrychat;

import java.awt.GridBagConstraints;

import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;



public class ClientWindow extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textMessage;
	private JTextArea textArea;
	private DefaultCaret caret;
	private Client client;
	
	private String name;
	
	private ErrorWindow errorW;
	
	private ErrorWindowListener eWListener;
	private volatile boolean eWActive;
	
	public ClientWindow(Client client) {
		setTitle("Jerry Chat Client");
		createWindow();
		eWActive = false;
		// set error window listener
		eWListener = new ErrorWindowListener() {
			public void onWindowExit() {
				Login frame = new Login();
				frame.setVisible(true);
				dispose();
			}
		};
		
		this.name = client.getName();
		this.client = client;
		textArea.append(name + " you are connected to " + client.getAddress() + " on port " + client.getPort());
		
		// when client receives message from server
		OnMessageListener receiveListener = new OnMessageListener() {
			@Override
			public void onMessageRecieved(String message) {
				console(message);
			}
			@Override
			public String onBeforeMessageSent(String message) {								
				return insertLinesToString(message, 100);
			}
			@Override
			public void onCantConnectToServer(String error) {
				displayError(error, eWListener);
			}
			@Override
			public void onConnectionLost(String error) {
				
				displayError(error, eWListener);
			}
		};
		client.registerRequestListener(receiveListener);
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
	
	/**
	 * This function inserts new line characters into
	 * a given string at the give line length index
	 * @param string striing to insert \n new lines in
	 * @param lineLength length of line before adding new line
	 * @return the string with new line characters
	 */
	private String insertLinesToString(String string, int lineLength) {
		int  lineBreaks = string.length()/lineLength, startIndex = 0, endIndex = 0;
		String temp = "";
		for (int count = 0; count < lineBreaks; count++) {
			endIndex += lineLength;
			temp += string.subSequence(startIndex, endIndex)+"\n";
			startIndex+=lineLength;
		}
		temp+=string.substring(startIndex);		
		return temp;
	}
	
	private void createWindow() {
		// make sure UI looks native
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(880, 550);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{28, 815, 30, 7};
		gridBagLayout.rowHeights = new int[]{35, 475, 40};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		
		// Make textArea able to scroll
		JScrollPane scroll = new JScrollPane(textArea);
		
		GridBagConstraints scrollConstraints = new GridBagConstraints();
		scrollConstraints.insets = new Insets(0, 0, 5, 5);
		scrollConstraints.fill = GridBagConstraints.BOTH;
		scrollConstraints.gridx = 0;
		scrollConstraints.gridy = 0;
		scrollConstraints.gridwidth = 3;
		scrollConstraints.gridheight = 2;
		scrollConstraints.insets = new Insets(0, 5, 0, 0);
		
		getContentPane().add(scroll, scrollConstraints);
		
		caret = (DefaultCaret) textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		
		textMessage = new JTextField();
		textMessage.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				
				// limit textbox to 100 characters
				if (textMessage.getText().length() > 500) {
					textMessage.setText(textMessage.getText().substring(0, 500));
				}
				
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					sendMessage(textMessage.getText());
				}
			}
		});
		GridBagConstraints gbc_textMessage = new GridBagConstraints();
		gbc_textMessage.insets = new Insets(0, 0, 0, 5);
		gbc_textMessage.fill = GridBagConstraints.HORIZONTAL;
		gbc_textMessage.gridx = 0;
		gbc_textMessage.gridy = 2;
		gbc_textMessage.gridwidth = 2;
		contentPane.add(textMessage, gbc_textMessage);
		textMessage.setColumns(10);
		
		JButton sendMessageButton = new JButton("Send");
		sendMessageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendMessage(textMessage.getText());
			}
		});
		
		GridBagConstraints gbc_sendMessageButton = new GridBagConstraints();
		gbc_sendMessageButton.insets = new Insets(0, 0, 0, 5);
		gbc_sendMessageButton.gridx = 2;
		gbc_sendMessageButton.gridy = 2;
		contentPane.add(sendMessageButton, gbc_sendMessageButton);
		
		
		textMessage.requestFocusInWindow();
		
		setVisible(true);
	}
	
	public void console(String message) {
		textArea.append("\n" + message);
	}
	
	private void sendMessage(String message) {
		if (!message.isEmpty()) {
			client.send(message);
			textMessage.setText("");
		}
	}
	
	
}
