package rhythmlux;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;
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
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == ui.cmbx_keyBindingsKeys) {
			ui.updateKeyBindingsButtonsPanel();
		} else if (e.getSource() == ui.btn_networkStart) {
			toggleListening();
		} else if (e.getSource() == ui.btn_networkUpdateESP32) {
			sendKeysArray(ui.getButtonKeyCodes());
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
			handleError("Error sending keys array: " + e.getMessage());
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
			int maxValue = source.getMaximum(); // Get the maximum value of the slider
			int sliderValue = maxValue - source.getValue(); // Reverse the slider value
			sendFadeValue(sliderValue);
		}
	}

}
