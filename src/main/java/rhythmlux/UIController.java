package rhythmlux;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

public class UIController implements NativeKeyListener, ActionListener {

	private static UIController instance;
	private UI ui;

	private boolean listening = false;
	private DatagramSocket socket;

	private Set<Integer> pressedKeys = new HashSet<>();
	private InetAddress address;
	private int port;

	public UIController(UI ui) {
		this.ui = ui;

		attachListeners();
		try {
			GlobalScreen.registerNativeHook();
		} catch (NativeHookException ex) {
			System.err.println("Failed to register native hook: " + ex.getMessage());
			System.exit(1);
		}

		GlobalScreen.addNativeKeyListener(this);

	}

	public static UIController getInstance(UI ui) {
		if (instance == null) {
			instance = new UIController(ui);
		}
		return instance;
	}

	private void attachListeners() {
		ui.btn_StartStop.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == ui.btn_StartStop) {
			toggleListening();
		}
	}

	private void toggleListening() {
		if (!listening) {
			try {
				// Create a DatagramSocket
				socket = new DatagramSocket();
				address = InetAddress.getByName(ui.IPField.getText());
				port = Integer.parseInt(ui.portField.getText());

				listening = true;
				ui.btn_StartStop.setText("Stop");
				JOptionPane.showMessageDialog(ui, "Established UDP connection with " + address + " on port " + port,
						"Connection Established", JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception e) {
				// Notify the user about the error
				JOptionPane.showMessageDialog(ui,
						"Failed to establish connection with " + address + " on port " + port
								+ ". Check your IP and port and try again.",
						"Connection Failed", JOptionPane.ERROR_MESSAGE);
				// Reset the button text
				ui.btn_StartStop.setText("Start");
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
			ui.btn_StartStop.setText("Start");
		}
	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent e) {
		if (listening) {
			int keyCode = e.getRawCode();
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

	@Override
	public void nativeKeyReleased(NativeKeyEvent e) {
		if (listening) {
			int keyCode = e.getRawCode();
			boolean keyPressed = false;
			sendKeyState(keyCode, keyPressed);
			// Remove the key from the set of pressed keys
			pressedKeys.remove(keyCode);

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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}