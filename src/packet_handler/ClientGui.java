package packet_handler;
import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Observable;
import java.util.Observer;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import java.awt.ScrollPane;

public class ClientGui extends JFrame implements Observer {
	//test comment
	UDPClient udpClient;
	RunUDP runUdp;
	
	private JPanel contentPane;
	
	protected File selectedFile;
	private JTextField fileNameField;
	private JButton btnSendFile;
	
	
	//private String messageReceived = "";
	//private String messageSend = "";
	
	
	
	private JTextArea feedBackArea;

	private ScrollPane scrollPane;
	
	private JTextField packetLossTextField;
	private double packetLossPercentage = 0.0;
	
	private JTextField corruptionTextField;
	private double corruptionPercentage = 0.0;
	


	/**
	 * Launch the application.
	 */
	/*public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI frame = new GUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}*/

	/**
	 * Create the frame.
	 */
	public ClientGui(UDPClient udpClient, RunUDP runUdp) {
		setTitle("Client");
		this.udpClient = udpClient;
		this.runUdp = runUdp;
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 603, 606);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		feedBackArea = new JTextArea();
		feedBackArea.setBounds(10, 229, 568, 328);
		
		JLabel lblSelectFile = new JLabel("File:");
		lblSelectFile.setBounds(20, 11, 81, 14);
		contentPane.add(lblSelectFile);
		
		JButton btnChooseFile = new JButton("Select");
		btnChooseFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//File chooser, starts in working folder
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new java.io.File("."));
				int returnValue = fileChooser.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					//when you selected a file, store it
					selectedFile = fileChooser.getSelectedFile();
					//set the GUI nameField to show which file was selected
					fileNameField.setText(selectedFile.getName());
					}
			}
		});
		btnChooseFile.setBounds(20, 26, 98, 45);
		contentPane.add(btnChooseFile);
		
		fileNameField = new JTextField();
		fileNameField.setEditable(false);
		fileNameField.setBounds(128, 38, 135, 20);
		contentPane.add(fileNameField);
		fileNameField.setColumns(10);
		
		JButton btnSendFile = new JButton("Send File");
		btnSendFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				udpClient.setSelectedFile(selectedFile);
				double corruption_prob = Double.parseDouble(corruptionTextField.getText());
				double failure_prob = Double.parseDouble(packetLossTextField.getText());
				int packet_size = 1024; // FIXME - link to packet text field
				int timeout_interval = 1000; // FIXME - link to timeout text field
				runUdp.setParameters(failure_prob, corruption_prob,
						packet_size, timeout_interval);
				
				udpClient.run();
			}
		});
		btnSendFile.setBounds(20, 82, 98, 43);
		contentPane.add(btnSendFile);
		
		scrollPane = new ScrollPane();
		scrollPane.add(feedBackArea);
		scrollPane.setBounds(20, 186, 540, 372);
		contentPane.add(scrollPane);
		
		JLabel lblPacketLoss = new JLabel("Packet Loss (%):");
		lblPacketLoss.setBounds(325, 41, 98, 14);
		contentPane.add(lblPacketLoss);
		
		packetLossTextField = new JTextField();
		packetLossTextField.setBounds(433, 38, 86, 20);
		packetLossTextField.setText("0");
		contentPane.add(packetLossTextField);
		packetLossTextField.setColumns(10);
		
		JLabel lblCorruption = new JLabel("Corruption (%):");
		lblCorruption.setBounds(325, 84, 98, 14);
		contentPane.add(lblCorruption);
		
		corruptionTextField = new JTextField();
		corruptionTextField.setBounds(433, 82, 86, 20);
		corruptionTextField.setText("0");
		contentPane.add(corruptionTextField);
		corruptionTextField.setColumns(10);
		
		//contentPane.setVisible(true);
	}

	/*public String getMessageReceived() {
		return messageReceived;
	}*/

	public void setMessageReceived() {
		//messageReceived = messageBox.getText();
	}

	public File getSelectedFile() {
		return selectedFile;
	}

	public void setSelectedFile(File selectedFile) {
		this.selectedFile = selectedFile;
	}

	//FIXME add a "Sending: " message to the textArea for the string, we can just do a getText and print it, but for file it would be packet number
	@Override
	public void update(Observable arg0, Object message) {
		
		
		//print the received message to the textArea
		feedBackArea.append(message +"\n");
		//Scrolls with the incoming new data
		scrollPane.setScrollPosition(0,feedBackArea.getDocument().getLength());
		
	}
	
	public void addController(ActionListener controller){
		

	}
}