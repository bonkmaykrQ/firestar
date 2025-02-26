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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.ArrayList;

public class Bert implements ActionListener {
	public JFrame frame = new JFrame();
	private JPanel frameContainer;
	private JButton cancelbtn;
	private JButton downloadbtn;
	private JRadioButton baseCheck;
	private JRadioButton patchCheck1;
	private JRadioButton patchCheck2;
	private JCheckBox hdCheck;
	private JCheckBox furyCheck;
	private JCheckBox skipBaseCheck;
	private JPanel installedList;
	private JLabel ipcBase;
	private JLabel ipcPatch1;
	private JLabel ipcPatch2;
	private JLabel ipcDlcHD;
	private JLabel ipcDlcFury;
	private JPanel installedListMargin;
	private ButtonGroup radios = new ButtonGroup();

	private JFrame invoker;

	private boolean wilkinsDownloadFinished = false;

	public Bert(JFrame parent) {
		parent.setEnabled(false);
		this.invoker = parent;

		frame.add(frameContainer); // initialize window contents -- will be handled by IntelliJ IDEA

		frame.setSize(600, 300); // 1280 800
		frame.setTitle("Download Assets");
		frame.setResizable(false);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.setLayout(new GridLayout());
		frame.setLocationRelativeTo(parent);
		frame.setAlwaysOnTop(true);

		radios.add(baseCheck);
		radios.add(patchCheck1);
		radios.add(patchCheck2);

		cancelbtn.addActionListener(this);
		downloadbtn.addActionListener(this);
		baseCheck.addActionListener(this);
		patchCheck1.addActionListener(this);
		patchCheck2.addActionListener(this);

		refreshChecklist();

		frame.setIconImage(Main.windowIcon);

		frame.setVisible(true);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e)
			{
				parent.setEnabled(true);
				e.getWindow().dispose();
			}
		});

		if (!new File(Main.inpath + "pkg2zip.exe").exists() || !new File(Main.inpath + "psvpfsparser.exe").exists()) {
			JOptionPane.showMessageDialog(frame, "The decryption tool is missing.\nPlease select \"Get Dependencies\" in the Options menu.", "Error", JOptionPane.ERROR_MESSAGE);
			invoker.setEnabled(true);
			frame.dispose();
		}

		if (new File(Main.inpath + "data2.psarc").exists())
			skipBaseCheck.setEnabled(true);
	}

	public boolean reportWhenDownloaded(WilkinsCoffee setup) {
		while (frame.isActive() || !wilkinsDownloadFinished) {}
		if (wilkinsDownloadFinished) { // intellij please shut the everloving FUCK up DON'T do this again or i will skin you alive
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		if (actionEvent.getSource() == baseCheck)
			skipBaseCheck.setEnabled(new File(Main.inpath + "data.psarc").exists()); else
		if (actionEvent.getSource() == patchCheck1)
			skipBaseCheck.setEnabled(new File(Main.inpath + "data1.psarc").exists()); else
		if (actionEvent.getSource() == patchCheck2)
			skipBaseCheck.setEnabled(new File(Main.inpath + "data2.psarc").exists()); else
		if (actionEvent.getSource() == cancelbtn) {
			invoker.setEnabled(true);
			frame.dispose();
		} else if (actionEvent.getSource() == downloadbtn) {
			ArrayList<Main.ArcTarget> arcs = new ArrayList<Main.ArcTarget>();
			ArrayList<Main.ArcKey> keys = new ArrayList<Main.ArcKey>();

			// Always download predecessors to ensure no missing files
			// Probably not necessary since I will be adding selective extraction to Gonzo,
			// and throw errors when a lower PSARC doesn't exist, but there's no real reason not to.
			//
			// Of course, to avoid additional download times, we should probably
			// avoid adding to the download list if a file already exists, unless explicitly
			// selected (the exact radio button). The user can redownload one at a time
			// but Firestar will not overwrite existing PSARCs otherwise.
			if (baseCheck.isSelected()) { // I have the base game
				if (!skipBaseCheck.isSelected() || !new File(Main.inpath + "data.psarc").exists()) {
					arcs.add(Main.ArcTarget.BASE);
					keys.add(Main.ArcKey.BASE);
					System.out.println("Queued download of data (Game version 1.0)");
				}
			}
			if (patchCheck1.isSelected()) { // I updated it once after it came out and then didn't turn my vita on for thousands of years
				if (!new File(Main.inpath + "data.psarc").exists()) {
					arcs.add(Main.ArcTarget.BASE);
					keys.add(Main.ArcKey.BASE);
					System.out.println("Queued download of data (Game version 1.0)");
				}
				if (!skipBaseCheck.isSelected() || !new File(Main.inpath + "data1.psarc").exists()) {
					arcs.add(Main.ArcTarget.FIRST);
					keys.add(Main.ArcKey.FIRST);
					System.out.println("Queued download of data1 (Game version 1.01)");
				}
			}
			if (patchCheck2.isSelected()) { // I'm a normal fucking person
				if (!new File(Main.inpath + "data.psarc").exists()) {
					arcs.add(Main.ArcTarget.BASE);
					keys.add(Main.ArcKey.BASE);
					System.out.println("Queued download of data (Game version 1.0)");
				}
				if (!new File(Main.inpath + "data1.psarc").exists()) {
					arcs.add(Main.ArcTarget.FIRST);
					keys.add(Main.ArcKey.FIRST);
					System.out.println("Queued download of data1 (Game version 1.01)");
				}
				if (!skipBaseCheck.isSelected() || !new File(Main.inpath + "data2.psarc").exists()) {
					arcs.add(Main.ArcTarget.LATEST);
					keys.add(Main.ArcKey.LATEST);
					System.out.println("Queued download of data2 (Game version 1.04)");
				}
			}

			// DLC are installed arbitrarily, separate of the game, and can remain as checkboxes with the old logic.
			// Likewise, the old overwrite policy applies. If the user selects these, it WILL download with no question.
			if (hdCheck.isSelected()) {
				arcs.add(Main.ArcTarget.ADDON_HD);
				keys.add(Main.ArcKey.ADDON_HD);
				System.out.println("Queued download of dlc1 (HD DLC)");
			}
			if (furyCheck.isSelected()) {
				arcs.add(Main.ArcTarget.ADDON_HD_FURY);
				keys.add(Main.ArcKey.ADDON_HD_FURY);
				System.out.println("Queued download of dlc2 (Fury DLC)");
			}
			if (arcs.isEmpty()) {
				frame.setVisible(false);
				JOptionPane.showMessageDialog(invoker, "Select one or more asset packs.", "Error", JOptionPane.ERROR_MESSAGE);
				frame.setVisible(true);
				return;
			}

			frame.dispose();
			invoker.setVisible(false);

			Thread downloaderPopupThread = new Thread(new Runnable() { // run on separate thread to prevent GUI freezing
				@Override
				public void run() {
					for (Main.ArcTarget type : arcs) {
						String key = "";
						String arcname = "";
						switch (type) {
							case BASE :
								key = Main.ArcKey.BASE.toString();
								arcname = "Base game";
								break;
							case FIRST :
								key = Main.ArcKey.FIRST.toString();
								arcname = "Update 1.01";
								break;
							case LATEST :
								key = Main.ArcKey.LATEST.toString();
								arcname = "Update 1.04";
								break;
							case ADDON_HD :
								key = Main.ArcKey.ADDON_HD.toString();
								arcname = "HD Add-On Pack";
								break;
							case ADDON_HD_FURY :
								key = Main.ArcKey.ADDON_HD_FURY.toString();
								arcname = "Fury Add-On Pack";
								break;
						}
						if (key.isEmpty()) {
							System.out.println("Internal Error: Bert got dementia. Get a programmer!");
							JOptionPane.showMessageDialog(invoker, "Internal Error: Bert got dementia. Get a programmer!", "Fatal Error", JOptionPane.ERROR_MESSAGE);
							return;
						}

						// download file
						Fozzie downloaderHandler = new Fozzie();
						boolean downloader = downloaderHandler.DownloadFile(type.toString(), Main.inpath, "asset.pkg", arcname);

						if (!downloader) {
							// cleanup
							new File(Main.inpath + "asset.pkg").delete();

							// restore controls
							invoker.setEnabled(true);
							invoker.setVisible(true);
							invoker.toFront();
							invoker.repaint();
							return;
						}

						// dump contents
						System.out.println("Extracting " + arcname);
						Fozzie popup = new Fozzie();
						popup.displayTextOnly("Extracting " + arcname + "...", "Extracting");
						try {
							Process p = Main.exec(new String[]{Main.inpath + "pkg2zip.exe", "-x", "asset.pkg", key.toString()}, Main.inpath);
							InputStream debugin = new BufferedInputStream(p.getInputStream());
							OutputStream debugout = new BufferedOutputStream(System.out);
							int c;
							while ((c = debugin.read()) != -1) {
								debugout.write(c);
							}
							debugin.close();
							debugout.close();
							p.waitFor();
						} catch (Exception e) {
							System.out.println(e.getMessage());
							JOptionPane.showMessageDialog(invoker, "CRITICAL FAILURE: " + e.getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
							new File(Main.inpath + "asset.pkg").delete();
							invoker.setEnabled(true);
							invoker.setVisible(true);
							invoker.toFront();
							invoker.repaint();
							return;
						}

						// decrypt
						System.out.println("Decrypting asset.pkg");
						String extracted;
						String name;
						if (type == Main.ArcTarget.BASE) {
							extracted = "app/PCSA00015";
							name = "data.psarc";
						} else if (type == Main.ArcTarget.FIRST) {
							extracted = "patch/PCSA00015";
							name = "data1.psarc";
						} else if (type == Main.ArcTarget.LATEST) {
							extracted = "patch/PCSA00015";
							name = "data2.psarc";
						} else if (type == Main.ArcTarget.ADDON_HD) {
							extracted = "addcont/PCSA00015/DLC1W2048PACKAGE";
							name = "dlc1.psarc";
						} else if (type == Main.ArcTarget.ADDON_HD_FURY) { // this is not "always true" - intellij is actually very dumj
							extracted = "addcont/PCSA00015/DLC2W2048PACKAGE";
							name = "dlc2.psarc";
						} else {
							System.out.println("Internal Error: Bert got dementia. Get a programmer!");
							JOptionPane.showMessageDialog(invoker, "Internal Error: Bert got dementia. Get a programmer!", "Fatal Error", JOptionPane.ERROR_MESSAGE);
							new File(Main.inpath + "asset.pkg").delete();
							Main.deleteDir(new File(Main.inpath + "app/"));
							Main.deleteDir(new File(Main.inpath + "patch/"));
							Main.deleteDir(new File(Main.inpath + "addcont/"));
							invoker.setEnabled(true);
							invoker.setVisible(true);
							invoker.toFront();
							invoker.repaint();
							return;
						}

						popup.setText("<html>Decrypting protected PFS:<br/>" + extracted + "</html>", "Decrypting");

						try {
							Process p = Main.exec(new String[]{Main.inpath + "psvpfsparser.exe", "-i", extracted, "-o", "./temp/", "-z", key.toString(), "-f", "cma.henkaku.xyz"}, Main.inpath);
							InputStream debugin = new BufferedInputStream(p.getInputStream());
							OutputStream debugout = new BufferedOutputStream(System.out);
							int c;
							while ((c = debugin.read()) != -1) {
								debugout.write(c);
							}
							debugin.close();
							debugout.close();
							p.waitFor();
						} catch (Exception e) {
							System.out.println(e.getMessage());
							JOptionPane.showMessageDialog(invoker, "CRITICAL FAILURE: " + e.getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
							new File(Main.inpath + "asset.pkg").delete();
							Main.deleteDir(new File(Main.inpath + "app/"));
							Main.deleteDir(new File(Main.inpath + "patch/"));
							Main.deleteDir(new File(Main.inpath + "addcont/"));
							invoker.setEnabled(true);
							invoker.setVisible(true);
							invoker.toFront();
							invoker.repaint();
							return;
						}

						popup.setText("Deleting temporary files...", "Cleaning Up");

						// stage & cleanup
						System.out.println("Cleaning up");
						new File(Main.inpath + "asset.pkg").delete();
						new File(Main.inpath + "temp/PSP2/" + name).renameTo(new File(Main.inpath + name));
						Main.deleteDir(new File(Main.inpath + "app/"));
						Main.deleteDir(new File(Main.inpath + "patch/"));
						Main.deleteDir(new File(Main.inpath + "addcont/"));
						Main.deleteDir(new File(Main.inpath + "temp/"));

						popup.destroyDialog();
					}

					// restore controls
					JOptionPane.showMessageDialog(frame, "Assets downloaded successfully.", "Download Complete", JOptionPane.INFORMATION_MESSAGE);
					invoker.setEnabled(true);
					invoker.setVisible(true);
					invoker.toFront();
					invoker.repaint();
					wilkinsDownloadFinished = true;
				}
			});
			downloaderPopupThread.start();
		}
	}

	private void refreshChecklist() {
		ImageIcon positive = new ImageIcon(Main.class.getResource("/lightPositive.png"));
		ImageIcon negative = new ImageIcon(Main.class.getResource("/lightNegative.png"));

		if(new File(Main.inpath + "data.psarc").exists())
			ipcBase.setIcon(positive); else ipcBase.setIcon(negative);
		if(new File(Main.inpath + "data1.psarc").exists())
			ipcPatch1.setIcon(positive); else ipcPatch1.setIcon(negative);
		if(new File(Main.inpath + "data2.psarc").exists())
			ipcPatch2.setIcon(positive); else ipcPatch2.setIcon(negative);
		if(new File(Main.inpath + "dlc1.psarc").exists())
			ipcDlcHD.setIcon(positive); else ipcDlcHD.setIcon(negative);
		if(new File(Main.inpath + "dlc2.psarc").exists())
			ipcDlcFury.setIcon(positive); else ipcDlcFury.setIcon(negative);
	}
}
