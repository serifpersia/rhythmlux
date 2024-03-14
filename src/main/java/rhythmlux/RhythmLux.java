package rhythmlux;

import com.formdev.flatlaf.FlatDarkLaf;

import java.awt.EventQueue;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

@SuppressWarnings("serial")
public class RhythmLux extends JFrame {

	private UI contentPane = new UI();

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(new FlatDarkLaf());
				RhythmLux frame = new RhythmLux();
				frame.setVisible(true);
			} catch (UnsupportedLookAndFeelException e) {
				e.printStackTrace();
			}
		});
	}

	public RhythmLux() {
		setTitle("RhythmLux");
		setSize(392, 364);
		setIconImage(new ImageIcon(getClass().getResource("/logo.png")).getImage());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);
		getContentPane().add(contentPane);

		new UIController(contentPane);
	}
}