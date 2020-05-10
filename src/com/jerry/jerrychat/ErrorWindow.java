package com.jerry.jerrychat;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;

public class ErrorWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JPanel contentPane;

	private ErrorWindowListener windowListener;
	
	/**
	 * Create the frame.
	 */
	public ErrorWindow(String error, ErrorWindowListener listener) {
		createWindow(error);
		this.windowListener = listener;
		
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				windowListener.onWindowExit();			
			}
			
		});
	}
	
	private void createWindow(String error) {
		// make sure UI looks native
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		setTitle("Error");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 354, 381);
		setSize(200, 180);
		setResizable(false);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel errorLabel = new JLabel("<html><div style='text-align: center; color: red;'><span style=\"font-weight: bold; color: black; \">Error: </span>" + error + "</div></html>");
		errorLabel.setBounds(12, 6, 175, 146);
		contentPane.add(errorLabel);
		setVisible(true);
	}
}
