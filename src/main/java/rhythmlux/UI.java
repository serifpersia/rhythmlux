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
import javax.swing.JComboBox;
import java.awt.FlowLayout;

@SuppressWarnings("serial")
public class UI extends JPanel {

	JComboBox<String> cmbx_keyBindingsKeys;
	JButton btn_StartStop;
	JTextField IPField;
	JTextField portField;

	public UI() {
		init();

	}

	private void init() {
		createNetworkPanel();
	}

	private void createNetworkPanel() {
		setLayout(new BorderLayout(0, 0));

		JLabel lb_Network = new JLabel("Network");
		lb_Network.setHorizontalAlignment(SwingConstants.CENTER);
		lb_Network.setForeground(Color.WHITE);
		lb_Network.setFont(new Font("Tahoma", Font.PLAIN, 20));
		add(lb_Network, BorderLayout.NORTH);

		JPanel panel = new JPanel();
		add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));

		JPanel panel_2 = new JPanel();
		panel.add(panel_2);
		panel_2.setLayout(new GridLayout(2, 0, 0, 0));

		JPanel panel_4 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_4.getLayout();
		flowLayout.setHgap(20);
		panel_2.add(panel_4);

		JLabel lblNewLabel = new JLabel("IP:");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
		panel_4.add(lblNewLabel);

		IPField = new JTextField();
		IPField.setText("192.168.1.6");
		IPField.setFont(new Font("Tahoma", Font.PLAIN, 18));
		IPField.setHorizontalAlignment(SwingConstants.CENTER);
		panel_4.add(IPField);
		IPField.setColumns(10);

		JPanel panel_1 = new JPanel();
		panel_2.add(panel_1);

		JLabel lblNewLabel_1 = new JLabel("Port:");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 18));
		panel_1.add(lblNewLabel_1);

		portField = new JTextField();
		portField.setText("12345");
		portField.setFont(new Font("Tahoma", Font.PLAIN, 18));
		portField.setHorizontalAlignment(SwingConstants.CENTER);
		panel_1.add(portField);
		portField.setColumns(10);

		JPanel panel_3 = new JPanel();
		panel.add(panel_3, BorderLayout.SOUTH);

		btn_StartStop = new JButton("Start");
		btn_StartStop.setFont(new Font("Tahoma", Font.PLAIN, 18));
		panel_3.add(btn_StartStop);
	}

}