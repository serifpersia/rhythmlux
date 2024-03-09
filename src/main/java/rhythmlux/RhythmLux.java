package rhythmlux;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

@SuppressWarnings("serial")
public class RhythmLux extends JFrame implements NativeKeyListener {
	private JTextField ipField;
	private JTextField portField;
	private JButton startStopButton;

	private boolean listening = false;
	private DatagramSocket socket;
	private String IP = "192.168.1.6";
	private String Port = "12345";

	private Set<Integer> pressedKeys = new HashSet<>();
	private InetAddress address;
	private int port;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(RhythmLux::new);
	}

	public RhythmLux() {
		setTitle("RhythmLux");
		setSize(300, 200);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new GridLayout(4, 1));
		setLocationRelativeTo(null);

		ipField = new JTextField(IP);
		portField = new JTextField(Port);
		startStopButton = new JButton("Start");

		add(ipField);
		add(portField);
		add(startStopButton);

		startStopButton.addActionListener(e -> toggleListening());

		ipField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				IP = ipField.getText();
				System.out.println("IP: " + IP);
			}
		});

		portField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Port = portField.getText();
				System.out.println("Port: " + Port);
			}
		});

		setVisible(true);

		try {
			GlobalScreen.registerNativeHook();
		} catch (NativeHookException ex) {
			System.err.println("Failed to register native hook: " + ex.getMessage());
			System.exit(1);
		}

		GlobalScreen.addNativeKeyListener(this);
	}

	private void toggleListening() {
		if (!listening) {
			try {
				// Create a DatagramSocket
				socket = new DatagramSocket();
				address = InetAddress.getByName(IP);
				port = Integer.parseInt(Port);

				listening = true;
				startStopButton.setText("Stop");
				JOptionPane.showMessageDialog(this, "Established UDP connection with " + IP + " on port " + Port,
						"Connection Established", JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception e) {
				// Notify the user about the error
				JOptionPane.showMessageDialog(this,
						"Failed to establish connection with " + IP + " on port " + Port
								+ ". Check your IP and port and try again.",
						"Connection Failed", JOptionPane.ERROR_MESSAGE);
				// Reset the button text
				startStopButton.setText("Start");
				e.printStackTrace();
			}
		} else {
			// Close the socket
			try {
				if (socket != null && !socket.isClosed()) {
					socket.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				// Handle socket closure exception
			}
			listening = false;
			startStopButton.setText("Start");
		}
	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent e) {
		if (listening) {
			int keyCode = e.getKeyCode();

			// Check if the pressed key is one of the keys of interest: D, F, J, K
			if (keyCode == NativeKeyEvent.VC_D || keyCode == NativeKeyEvent.VC_F || keyCode == NativeKeyEvent.VC_J
					|| keyCode == NativeKeyEvent.VC_K) {

				// Check if the key is not already pressed
				if (!pressedKeys.contains(keyCode)) {
					boolean keyPressed = true;
					// Send a message indicating the key state change
					sendKeyState(keyCode, keyPressed);
					// Add the key to the set of pressed keys
					pressedKeys.add(keyCode);
				}
			}
		}
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent e) {
		if (listening) {
			int keyCode = e.getKeyCode();

			// Check if the released key is one of the keys of interest: D, F, J, K
			if (keyCode == NativeKeyEvent.VC_D || keyCode == NativeKeyEvent.VC_F || keyCode == NativeKeyEvent.VC_J
					|| keyCode == NativeKeyEvent.VC_K) {

				boolean keyPressed = false;
				// Send a message indicaddddting the key state change
				sendKeyState(keyCode, keyPressed);
				// Remove the key from the set of pressed keys
				pressedKeys.remove(keyCode);
			}
		}
	}

	private void sendKeyState(int keyCode, boolean keyPressed) {
		try {

			// Create the message as a byte array
			byte keyStateByte = (byte) (keyPressed ? 1 : 0);
			byte[] message = { (byte) keyCode, keyStateByte };

			// Create a DatagramPacket with the message, destination address, and port
			DatagramPacket packet = new DatagramPacket(message, message.length, address, port);

			// Send the packet
			socket.send(packet);

			System.out.println("Sent key state: Key " + keyCode + " is " + (keyPressed ? "pressed" : "released"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}