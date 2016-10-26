package packet_handler;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import java.awt.ScrollPane;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;

public class ServerGui extends JFrame implements Observer {

	private JPanel contentPane;
	private JTextArea feedBackArea;
	private ScrollPane scrollPane;

	private int packetLossPercentage = 0;

	/**
	 * Create the frame.
	 */
	public ServerGui() {
		setTitle("Server");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 453, 538);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		feedBackArea = new JTextArea();
		feedBackArea.setBounds(10, 229, 568, 328);

		scrollPane = new ScrollPane();
		scrollPane.setBounds(10, 94, 417, 395);
		scrollPane.add(feedBackArea);
		contentPane.add(scrollPane);
	}

	/**
	 * @return feedBackArea
	 */
	public JTextArea getFeedBackArea() {
		return feedBackArea;
	}

	@Override
	public void update(Observable arg0, Object message) {

		// print the received message to the textArea
		feedBackArea.append(message + "\n");

		// Scrolls with the incoming new data
		scrollPane.setScrollPosition(0, feedBackArea.getDocument().getLength());

	}

	/**
	 * @return packetLossPercentage
	 */
	public int getPacketLossPercentage() {

		return packetLossPercentage;
	}

	/**
	 * @param packetLossPercentage
	 */
	public void setPacketLossPercentage(int packetLossPercentage) {
		this.packetLossPercentage = packetLossPercentage;
	}

}
