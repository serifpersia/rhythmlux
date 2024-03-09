package rhythmlux;

import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import javax.swing.JComboBox;

@SuppressWarnings("serial")
public class UI extends JPanel {
	private JTextField txtField_networkIP;
	private JTextField txtField_networkPort;

	JButton btn_networkSet;
	private JButton btn_networkStart;
	private JButton btn_networkHelp;

	JComboBox<String> cmbx_keyBindingsKeys;
	private JPanel KeysButtonsPanel;

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
		lb_Network.setForeground(Color.WHITE);
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
		txtField_networkIP.setText("192.168.1.6");
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

		btn_networkSet = new JButton("Set");
		btn_networkSet.setFont(new Font("Tahoma", Font.PLAIN, 14));
		UtilButtons_Panel.add(btn_networkSet);

		btn_networkStart = new JButton("Start");
		btn_networkStart.setFont(new Font("Tahoma", Font.PLAIN, 14));
		UtilButtons_Panel.add(btn_networkStart);

		btn_networkHelp = new JButton("Help");
		btn_networkHelp.setFont(new Font("Tahoma", Font.PLAIN, 14));
		UtilButtons_Panel.add(btn_networkHelp);
	}

	private void createKeyBindingsPanel() {
		JPanel keyBindingsPanel = new JPanel();
		add(keyBindingsPanel);
		keyBindingsPanel.setLayout(new BorderLayout(0, 0));

		JLabel lb_KeyBindings = new JLabel("Key Bindings");
		lb_KeyBindings.setHorizontalAlignment(SwingConstants.CENTER);
		lb_KeyBindings.setForeground(Color.WHITE);
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

		KeysButtonsPanel = new JPanel();
		FlowLayout fl_KeysButtonsPanel = (FlowLayout) KeysButtonsPanel.getLayout();
		fl_KeysButtonsPanel.setVgap(15);
		keyBindingsInnerPanel.add(KeysButtonsPanel, BorderLayout.CENTER);

		updateKeyBindingsButtonsPanel();
	}

	public void updateKeyBindingsButtonsPanel() {
		KeysButtonsPanel.removeAll();

		String selectedItem = (String) cmbx_keyBindingsKeys.getSelectedItem();
		int numKeys = Integer.parseInt(selectedItem.substring(0, selectedItem.length() - 1));

		String[] keyLabels = getKeyLabels(numKeys);

		for (String label : keyLabels) {
			JButton button = new JButton(label);
			button.setFont(new Font("Tahoma", Font.PLAIN, 14));
			KeysButtonsPanel.add(button);
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
			labels = new String[] { "A", "S", "D", "F", "J", "K", "L" };
			break;
		case 8:
			labels = new String[] { "A", "S", "D", "F", "G", "H", "J", "K" };
			break;
		case 9:
			labels = new String[] { "A", "S", "D", "F", "G", "H", "J", "K", "L" };
			break;
		case 10:
			labels = new String[] { "A", "S", "D", "F", "G", "H", "J", "K", "L", ";" };
			break;
		}
		return labels;
	}
}
