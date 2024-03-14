package rhythmlux;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

public class UIController implements NativeKeyListener, ActionListener, ChangeListener {

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
			handleError("Failed to register native hook: " + ex.getMessage());
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
		ui.cmbx_keyBindingsKeys.addActionListener(this);
		ui.btn_networkStart.addActionListener(this);
		ui.btn_networkUpdateESP32.addActionListener(this);
		ui.sld_Fade.addChangeListener(this);
		ui.btn_networkHelp.addActionListener(this);
		ui.btn_networkScan.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == ui.cmbx_keyBindingsKeys) {
			ui.updateKeyBindingsButtonsPanel();
		} else if (e.getSource() == ui.btn_networkStart) {
			toggleListening();
		} else if (e.getSource() == ui.btn_networkUpdateESP32) {
			sendKeysArray(ui.getButtonKeyCodes());
		} else if (e.getSource() == ui.btn_networkHelp) {
			handleHelpButton();
		} else if (e.getSource() == ui.btn_networkScan) {
			scanForESP32UDP();
		}
	}

	private void toggleListening() {
		if (!listening) {
			try {
				socket = new DatagramSocket();
				address = InetAddress.getByName(ui.txtField_networkIP.getText());
				port = Integer.parseInt(ui.txtField_networkPort.getText());
				listening = true;
				ui.btn_networkStart.setText("Stop");
				showInfoMessage("Established UDP connection with " + ui.txtField_networkIP.getText() + " on port "
						+ ui.txtField_networkPort.getText(), "Connection Established");
				sendKeysArray(ui.getButtonKeyCodes());
			} catch (Exception e) {
				handleError("Failed to establish connection with " + ui.txtField_networkIP.getText() + " on port "
						+ ui.txtField_networkPort.getText() + ". Check your IP and port and try again.");
				ui.btn_networkStart.setText("Start");
			}
		} else {
			closeSocket();
			listening = false;
			ui.btn_networkStart.setText("Start");
		}
	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent e) {
		if (listening) {
			int keyCode = e.getRawCode();
			int[] buttonsKeyCode = ui.getButtonKeyCodes();
			if (containsKey(buttonsKeyCode, keyCode) && !pressedKeys.contains(keyCode)) {
				boolean keyPressed = true;
				sendKeyState(keyCode, keyPressed);
				pressedKeys.add(keyCode);
			}
		}
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent e) {
		if (listening) {
			int keyCode = e.getRawCode();
			int[] buttonsKeyCode = ui.getButtonKeyCodes();
			if (containsKey(buttonsKeyCode, keyCode)) {
				boolean keyPressed = false;
				sendKeyState(keyCode, keyPressed);
				pressedKeys.remove(keyCode);
			}
		}
	}

	private boolean containsKey(int[] keys, int keyCode) {
		for (int key : keys) {
			if (key == keyCode) {
				return true;
			}
		}
		return false;
	}

	private void sendKeysArray(int[] keys) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			for (int key : keys) {
				dos.writeByte(key);
			}
			byte[] message = baos.toByteArray();
			DatagramPacket packet = new DatagramPacket(message, message.length, address, port);
			socket.send(packet);
		} catch (Exception e) {
			handleError("Error sending keys mode: " + e.getMessage());
		}
	}

	private void sendFadeValue(int fadeValue) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeByte(fadeValue);

			byte[] message = baos.toByteArray();
			DatagramPacket packet = new DatagramPacket(message, message.length, address, port);
			socket.send(packet);
		} catch (Exception e) {
			handleError("Error sending fade value: " + e.getMessage());
		}
	}

	private void sendKeyState(int keyCode, boolean keyPressed) {
		try {
			byte keyStateByte = (byte) (keyPressed ? 1 : 0);
			byte[] message = { (byte) keyCode, keyStateByte };
			DatagramPacket packet = new DatagramPacket(message, message.length, address, port);
			socket.send(packet);
		} catch (Exception e) {
			handleError("Error sending key state: " + e.getMessage());
		}
	}

	private void closeSocket() {
		try {
			if (socket != null && !socket.isClosed()) {
				socket.close();
			}
		} catch (Exception e) {
			handleError("Error closing socket: " + e.getMessage());
		}
	}

	private void handleError(String message) {
		JOptionPane.showMessageDialog(ui, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	private void showInfoMessage(String message, String title) {
		JOptionPane.showMessageDialog(ui, message, title, JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
		if (!source.getValueIsAdjusting()) {
			int maxValue = source.getMaximum();
			int sliderValue = maxValue - source.getValue();
			sendFadeValue(sliderValue);
		}
	}

	public void handleHelpButton() {
		String[] helpTips = { "Enter correct ESP32 IP and Port before trying to press Start button.",
				"Default Port is 12345.",
				"To change key bindings press the button with mouse click and press key you want.",
				"Chosen default key bindings key mode will be used when pressing Start button, to update esp32 board with new mode selected & new custom key bindings press ESP32 Update button",
				"Adjust fade rate with Fade slider use Update button to update esp32 board with new fade value." };

		JDialog dialog = new JDialog((Frame) null, "Help Tips", true);
		JPanel panel = new JPanel(new BorderLayout());

		JLabel helpTipLabel = new JLabel("<html><p>" + helpTips[0] + "</p></html>");
		helpTipLabel.setHorizontalAlignment(JLabel.CENTER);
		panel.add(helpTipLabel, BorderLayout.CENTER);

		JButton nextButton = new JButton("1/" + helpTips.length);
		nextButton.addActionListener(new ActionListener() {
			private int currentTipIndex = 0;

			@Override
			public void actionPerformed(ActionEvent e) {
				currentTipIndex = (currentTipIndex + 1) % helpTips.length;
				helpTipLabel.setText("<html><p>" + helpTips[currentTipIndex] + "</p></html>");
				nextButton.setText((currentTipIndex + 1) + "/" + helpTips.length);
			}
		});

		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(nextButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		dialog.add(panel);
		dialog.setSize(380, 150);
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}

	private void scanForESP32UDP() {
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
			socket.setBroadcast(true);

			InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
			int esp32Port = 12345;

			byte[] requestData = "ScanRequest".getBytes();
			DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, broadcastAddress,
					esp32Port);

			socket.send(requestPacket);
			System.out.println("Broadcast scan request sent.");

			byte[] responseData = new byte[1024];
			DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length);

			socket.setSoTimeout(1000);
			while (true) {
				socket.receive(responsePacket);
				String esp32IPAddress = responsePacket.getAddress().getHostAddress();
				ui.txtField_networkIP.setText(esp32IPAddress);
				socket.close();
				break;
			}
		} catch (Exception e) {
			handleError("Error scanning for ESP32 devices: " + e.getMessage());
		} finally {
			if (socket != null && !socket.isClosed()) {
				socket.close();
			}
		}
	}
}