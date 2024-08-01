/*
 *     Firestar Mod Manager
 *     Copyright (C) 2024  bonkmaykr
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see https://www.gnu.org/licenses/.
 */

import javax.imageio.*;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class Rowlf {
	JFrame frame = new JFrame();
	JPanel frameContainer;
	Image logo;
	JLabel picLabel;
	private JTextField informationText;
	private JLabel versionLabel;
	private JLabel environmentLabel;

	public Rowlf(JFrame parent) {
		try {
			logo = ImageIO.read(Main.class.getResourceAsStream("/logo.png")).getScaledInstance(333, 100, Image.SCALE_SMOOTH);
		} catch (IOException e) {
			System.out.println("ERROR: Failed to open About screen because we couldn't find an image needed to display the page.");
			throw new RuntimeException(e);
		}
		frame.setIconImage(Main.windowIcon);

		//frame.add(picLabel);
		frame.add(frameContainer); // initialize window contents -- will be handled by IntelliJ IDEA
		picLabel.setIcon(new ImageIcon(logo));picLabel.setText("");

		informationText.getDocument().putProperty("filterNewlines", Boolean.FALSE);
		informationText.setHorizontalAlignment(JTextField.CENTER);
		informationText.setText("Created by bonkmaykr & Wirlaburla\n" +
				"\n" +
				"Special thanks to:\n" +
				"ThatOneBonk, ChaCheeChoo, and the AGRF community\n" +
				"Psygnosis / Studio Liverpool, SCE Europe\n" +
				"Yifan Lu & FailOverflow, TheRadziu, NoPayStation\n" +
				"and to all the PSVita hackers who made this possible");
		informationText.setHighlighter(null);
		informationText.getCaret().setVisible(false);
		informationText.setFocusable(false);

		versionLabel.setText(Main.vstr + " (" + Main.vcode + ")");
		//if (Main.isNightly) {versionLabel.setText(versionLabel.getText() + " built " + Main.dateOfCompile);}
		environmentLabel.setText("Running on Java " + System.getProperty("java.version") + " for " + System.getProperty("os.name"));

		// display window
		frame.setSize(400, 320);
		frame.setTitle("About Firestar");
		frame.setResizable(false);
		frame.setAlwaysOnTop(true);
		frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		frame.setLayout(new GridLayout());
		frame.setLocationRelativeTo(parent);
		frame.setVisible(true);
	}
}
