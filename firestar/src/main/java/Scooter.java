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

import javax.swing.*;
import java.awt.*;

public class Scooter {
	private JFrame frame = new JFrame();
	public JProgressBar progressBar;
	private JPanel frameContainer;
	private JLabel label;

	public void showDialog(String title) {
		frame.add(frameContainer);
		frame.setSize(300, 100);
		frame.setTitle(title);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.setLayout(new GridLayout());
		frame.setLocationRelativeTo(null);
		frame.setAlwaysOnTop(true);
		frame.setIconImage(Main.windowIcon);
	
		progressBar.setStringPainted(true);
	
		frame.setVisible(true);
	}
	
	public void setProgressMin(int i) {
		progressBar.setMinimum(i);
	}
	
	public void setProgressValue(int i) {
		progressBar.setValue(i);
	}
	
	public void setProgressMax(int i) {
		progressBar.setMaximum(i);
	}
	
	public int getProgressMin() {
		return progressBar.getMinimum();
	}
	
	public int getProgressValue() {
		return progressBar.getValue();
	}
	
	public int getProgressMax() {
		return progressBar.getMaximum();
	}

	public void setText(String text) {
		label.setText(text);
	}

	public void destroyDialog() {
		frame.setVisible(false);
		frame.dispose();
	}
}
