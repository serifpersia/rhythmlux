package rhythmlux;

import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import java.awt.Font;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JTextField;
import java.awt.FlowLayout;
import javax.swing.JComboBox;
import javax.swing.JSlider;

@SuppressWarnings("serial")
public class UI extends JPanel {
	JTextField txtField_networkIP;
	JTextField txtField_networkPort;

	JComboBox<String> cmbx_keyBindingsKeys;
	private JPanel KeysButtonsPanel;

	private JButton[] keyBindingsButtons;

	ArrayList<Integer> keyCodes = new ArrayList<>(Arrays.asList());
	JButton btn_networkStart;
	JButton btn_networkUpdateESP32;
	JButton btn_networkHelp;
	JSlider sld_Fade;
	JButton btn_networkScan;

	public UI() {
		init();

	}

	private void init() {
		setLayout(new GridLayout(2, 0, 0, 0));
		createNetworkPanel();
		createKeyBindingsPanel();
	}

	private void createNetworkPanel() {
		JPanel networkPanel = new JPanel();
		add(networkPanel);
		networkPanel.setLayout(new BorderLayout(0, 0));

		JLabel lb_Network = new JLabel("Network");
		lb_Network.setForeground(Color.LIGHT_GRAY);
		lb_Network.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lb_Network.setHorizontalAlignment(SwingConstants.CENTER);
		networkPanel.add(lb_Network, BorderLayout.NORTH);

		JPanel innerNetworkPanel = new JPanel();
		networkPanel.add(innerNetworkPanel, BorderLayout.CENTER);
		innerNetworkPanel.setLayout(new GridLayout(2, 0, 0, 0));

		JPanel IP_Panel = new JPanel();
		FlowLayout fl_IP_Panel = (FlowLayout) IP_Panel.getLayout();
		fl_IP_Panel.setHgap(40);
		innerNetworkPanel.add(IP_Panel);

		JLabel lb_networkIPLabel = new JLabel("IP:");
		lb_networkIPLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lb_networkIPLabel.setHorizontalAlignment(SwingConstants.CENTER);
		IP_Panel.add(lb_networkIPLabel);

		txtField_networkIP = new JTextField();
		txtField_networkIP.setText("scan to get IP");
		txtField_networkIP.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtField_networkIP.setHorizontalAlignment(SwingConstants.CENTER);
		txtField_networkIP.setColumns(10);
		IP_Panel.add(txtField_networkIP);

		JPanel Port_Panel = new JPanel();
		FlowLayout fl_Port_Panel = (FlowLayout) Port_Panel.getLayout();
		fl_Port_Panel.setHgap(25);
		innerNetworkPanel.add(Port_Panel);

		JLabel lb_networkPortLabel = new JLabel("Port:");
		lb_networkPortLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lb_networkPortLabel.setHorizontalAlignment(SwingConstants.CENTER);
		Port_Panel.add(lb_networkPortLabel);

		txtField_networkPort = new JTextField();
		txtField_networkPort.setText("12345");
		txtField_networkPort.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtField_networkPort.setHorizontalAlignment(SwingConstants.CENTER);
		txtField_networkPort.setColumns(10);
		Port_Panel.add(txtField_networkPort);

		JPanel UtilButtons_Panel = new JPanel();
		networkPanel.add(UtilButtons_Panel, BorderLayout.SOUTH);
		UtilButtons_Panel.setLayout(new GridLayout(2, 0, 0, 0));

		JPanel panel = new JPanel();
		UtilButtons_Panel.add(panel);
		
		btn_networkScan = new JButton("Scan");
		btn_networkScan.setFont(new Font("Tahoma", Font.PLAIN, 14));
		panel.add(btn_networkScan);

		btn_networkStart = new JButton("Start");
		btn_networkStart.setFont(new Font("Tahoma", Font.PLAIN, 14));
		panel.add(btn_networkStart);

		btn_networkUpdateESP32 = new JButton("Update ESP32");
		btn_networkUpdateESP32.setFont(new Font("Tahoma", Font.PLAIN, 14));
		panel.add(btn_networkUpdateESP32);

		btn_networkHelp = new JButton("Help");
		btn_networkHelp.setFont(new Font("Tahoma", Font.PLAIN, 14));
		panel.add(btn_networkHelp);

		JPanel panel_1 = new JPanel();
		UtilButtons_Panel.add(panel_1);

		JLabel lb_Slider = new JLabel("Fade:");
		lb_Slider.setHorizontalAlignment(SwingConstants.CENTER);
		lb_Slider.setForeground(Color.LIGHT_GRAY);
		lb_Slider.setFont(new Font("Tahoma", Font.PLAIN, 18));
		panel_1.add(lb_Slider);

		sld_Fade = new JSlider();
		sld_Fade.setMaximum(120);
		sld_Fade.setMinorTickSpacing(4);
		sld_Fade.setSnapToTicks(true);
		sld_Fade.setFont(new Font("Tahoma", Font.PLAIN, 18));
		sld_Fade.setValue(64);
		panel_1.add(sld_Fade);
	}

	public void createKeyBindingsPanel() {
		JPanel keyBindingsPanel = new JPanel();
		add(keyBindingsPanel);
		keyBindingsPanel.setLayout(new BorderLayout(0, 0));

		JLabel lb_KeyBindings = new JLabel("Key Bindings");
		lb_KeyBindings.setHorizontalAlignment(SwingConstants.CENTER);
		lb_KeyBindings.setForeground(Color.LIGHT_GRAY);
		lb_KeyBindings.setFont(new Font("Tahoma", Font.PLAIN, 20));
		keyBindingsPanel.add(lb_KeyBindings, BorderLayout.NORTH);

		JPanel keyBindingsInnerPanel = new JPanel();
		keyBindingsPanel.add(keyBindingsInnerPanel, BorderLayout.CENTER);
		keyBindingsInnerPanel.setLayout(new BorderLayout(0, 0));

		JPanel KeysPanel = new JPanel();
		keyBindingsInnerPanel.add(KeysPanel, BorderLayout.NORTH);

		JLabel lb_keyBindingsLabel = new JLabel("Mode:");
		lb_keyBindingsLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lb_keyBindingsLabel.setFont(new Font("Dialog", Font.PLAIN, 14));
		KeysPanel.add(lb_keyBindingsLabel);

		cmbx_keyBindingsKeys = new JComboBox<>(new String[] { "4K", "5K", "6K", "7K", "8K", "9K", "10K" });
		cmbx_keyBindingsKeys.setFont(new Font("Dialog", Font.PLAIN, 14));
		KeysPanel.add(cmbx_keyBindingsKeys);

		FlowLayout fl_KeysButtonsPanel = new FlowLayout(FlowLayout.CENTER);
		fl_KeysButtonsPanel.setVgap(10);
		KeysButtonsPanel = new JPanel(fl_KeysButtonsPanel);
		keyBindingsInnerPanel.add(KeysButtonsPanel, BorderLayout.CENTER);

		updateKeyBindingsButtonsPanel();
	}

	public void updateKeyBindingsButtonsPanel() {
		KeysButtonsPanel.removeAll();

		String selectedItem = (String) cmbx_keyBindingsKeys.getSelectedItem();
		int numKeys = Integer.parseInt(selectedItem.substring(0, selectedItem.length() - 1));

		String[] keyLabels = getKeyLabels(numKeys);

		keyCodes.clear();
		switch (numKeys) {
		}

		keyBindingsButtons = new JButton[keyLabels.length];

		for (int i = 0; i < keyLabels.length; i++) {
			keyBindingsButtons[i] = new JButton(keyLabels[i]);
			keyBindingsButtons[i].setFont(new Font("Tahoma", Font.PLAIN, 14));
			keyBindingsButtons[i].putClientProperty("ID", i);
			KeysButtonsPanel.add(keyBindingsButtons[i]);
			keyBindingsButtons[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JButton clickedButton = (JButton) e.getSource();
					int buttonID = (int) clickedButton.getClientProperty("ID");
					clickedButton.setText("Press any key");

					for (JButton button : keyBindingsButtons) {
						button.setEnabled(false);
					}
					KeyboardFocusManager.getCurrentKeyboardFocusManager()
							.addKeyEventDispatcher(new KeyEventDispatcher() {
								@Override
								public boolean dispatchKeyEvent(KeyEvent e) {
									if (e.getID() == KeyEvent.KEY_PRESSED) {
										int keyCode = e.getKeyCode();
										System.out.println("Clicked button keyCode: " + keyCode);
										if (keyCode == KeyEvent.VK_SPACE) {
											clickedButton.setText("Space");
										} else {
											clickedButton.setText(String.valueOf((char) keyCode));
										}
										setButtonKeyCode(buttonID, keyCode);
										for (JButton button : keyBindingsButtons) {
											button.setEnabled(true);
										}
										KeyboardFocusManager.getCurrentKeyboardFocusManager()
												.removeKeyEventDispatcher(this);
									}
									return false;
								}
							});
				}
			});
		}
		revalidate();
		repaint();
	}

	private String[] getKeyLabels(int numKeys) {
		String[] labels = new String[numKeys];
		switch (numKeys) {
		case 4:
			labels = new String[] { "D", "F", "J", "K" };
			break;
		case 5:
			labels = new String[] { "D", "F", "G", "J", "K" };
			break;
		case 6:
			labels = new String[] { "S", "D", "F", "J", "K", "L" };
			break;
		case 7:
			labels = new String[] { "A", "S", "D", "Space", "J", "K", "L" };
			break;
		case 8:
			labels = new String[] { "A", "S", "D", "Space", "Space", "H", "J", "K" };
			break;
		case 9:
			labels = new String[] { "A", "S", "D", "F", "Space", "H", "J", "K", "L" };
			break;
		case 10:
			labels = new String[] { "A", "S", "D", "F", "Space", "Space", "J", "K", "L", "P" };
			break;
		}
		return labels;
	}

	public int[] getButtonIDs() {
		int[] buttonIDs = new int[keyBindingsButtons.length];
		for (int i = 0; i < keyBindingsButtons.length; i++) {
			buttonIDs[i] = (int) keyBindingsButtons[i].getClientProperty("ID");
		}
		return buttonIDs;
	}

	public void setButtonKeyCode(int buttonID, int keyCode) {
		if (buttonID >= 0 && buttonID < keyCodes.size()) {
			keyCodes.set(buttonID, keyCode);
		}
	}

	public int[] getButtonKeyCodes() {
		int[] keyCodesArray = new int[keyCodes.size()];
		for (int i = 0; i < keyCodes.size(); i++) {
			keyCodesArray[i] = keyCodes.get(i);
		}
		return keyCodesArray;
	}
}