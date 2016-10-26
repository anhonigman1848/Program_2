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
import javax.swing.SwingConstants;

public class ClientGui extends JFrame implements Observer {

	UDPClient udpClient;
	RunUDP runUdp;

	private JPanel contentPane;

	protected File selectedFile;
	private JTextField fileNameField;
	private JButton btnSendFile;

	private JTextArea feedBackArea;

	private ScrollPane scrollPane;

	private JTextField packetLossTextField;
	private double packetLossPercentage = 0.0;

	private JTextField corruptionTextField;
	private double corruptionPercentage = 0.0;

	private JTextField packetSizeTextField;
	private int packet_size = 0;

	private JTextField timeoutTextField;
	private int timeout_interval = 0;

	/**
	 * Create the frame.
	 */
	/**
	 * @param udpClient
	 * @param runUdp
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
				// File chooser, starts in working folder
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new java.io.File("."));
				int returnValue = fileChooser.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					// when you selected a file, store it
					selectedFile = fileChooser.getSelectedFile();
					// set the GUI nameField to show which file was selected
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

				int packet_size = Integer.parseInt(packetSizeTextField.getText());
				int timeout_interval = Integer.parseInt(timeoutTextField.getText());

				runUdp.setParameters(failure_prob, corruption_prob, packet_size, timeout_interval);

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
		lblPacketLoss.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPacketLoss.setBounds(306, 87, 120, 14);
		contentPane.add(lblPacketLoss);

		packetLossTextField = new JTextField();
		packetLossTextField.setBounds(427, 84, 86, 20);
		packetLossTextField.setText("0");
		contentPane.add(packetLossTextField);
		packetLossTextField.setColumns(10);

		JLabel lblCorruption = new JLabel("Corruption (%):");
		lblCorruption.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCorruption.setBounds(306, 114, 120, 14);
		contentPane.add(lblCorruption);

		corruptionTextField = new JTextField();
		corruptionTextField.setBounds(427, 112, 86, 20);
		corruptionTextField.setText("0");
		contentPane.add(corruptionTextField);
		corruptionTextField.setColumns(10);

		JLabel lblPacketSize = new JLabel("Packet Size:");
		lblPacketSize.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPacketSize.setBounds(306, 29, 120, 14);
		contentPane.add(lblPacketSize);

		packetSizeTextField = new JTextField();
		packetSizeTextField.setBounds(427, 26, 86, 20);
		packetSizeTextField.setText("1024");
		contentPane.add(packetSizeTextField);
		packetSizeTextField.setColumns(10);

		JLabel lblClientTimeoutms = new JLabel("Client Timeout (ms):");
		lblClientTimeoutms.setHorizontalAlignment(SwingConstants.RIGHT);
		lblClientTimeoutms.setBounds(306, 58, 120, 14);
		contentPane.add(lblClientTimeoutms);

		timeoutTextField = new JTextField();
		timeoutTextField.setBounds(427, 55, 86, 20);
		timeoutTextField.setText("1000");
		contentPane.add(timeoutTextField);
		timeoutTextField.setColumns(10);

	}

	
	/**
	 * Get the selected file
	 * @return selectedFile
	 */
	public File getSelectedFile() {
		return selectedFile;
	}

	/**
	 * Set the selected file
	 * @param selectedFile
	 */
	public void setSelectedFile(File selectedFile) {
		this.selectedFile = selectedFile;
	}

	
	@Override
	public void update(Observable arg0, Object message) {

		// print the received message to the textArea
		feedBackArea.append(message + "\n");
		// Scrolls with the incoming new data
		scrollPane.setScrollPosition(0, feedBackArea.getDocument().getLength());

	}

}