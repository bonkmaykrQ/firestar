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

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;

public class Bert implements ActionListener {
    BufferedImage windowIcon;
    public JFrame frame = new JFrame();
    private JPanel frameContainer;
    private JButton cancelbtn;
    private JButton downloadbtn;
    private JRadioButton baseRad;
    private JRadioButton patchRad;
    private JRadioButton hdRad;
    private JRadioButton furyRad;
    private ButtonGroup radios = new ButtonGroup();

    private JFrame invoker;

    public Bert(JFrame parent) {
        parent.setEnabled(false);
        this.invoker = parent;

        frame.add(frameContainer); // initialize window contents -- will be handled by IntelliJ IDEA

        frame.setSize(600, 300); // 1280 800
        frame.setTitle("Download Assets");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setLayout(new GridLayout());
        frame.setLocationRelativeTo(null);
        frame.setAlwaysOnTop(true);

        radios.add(baseRad);
        radios.add(patchRad);
        radios.add(hdRad);
        radios.add(furyRad);

        cancelbtn.addActionListener(this);
        downloadbtn.addActionListener(this);

        try {
            windowIcon = ImageIO.read(Main.class.getResourceAsStream("/titleIcon.png"));
            frame.setIconImage(windowIcon);
        } catch (IOException e) {
            System.out.println("ERROR: Failed to find /resources/titleIcon.png. Window will not have an icon.");
        }

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
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == cancelbtn) {
            invoker.setEnabled(true);
            frame.dispose();
        } else if (actionEvent.getSource() == downloadbtn) {
            int result = JOptionPane.showConfirmDialog(frame, "All existing PSARC dumps will be deleted and replaced by the new ones.\nThis download could take several minutes. Do you want to continue?", "Download Game Assets", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.NO_OPTION) {return;}
            System.out.println("User requested download of assets. Deleting existing PSARCs");
            new File(Main.inpath + "data.psarc").delete();
            new File(Main.inpath + "data01.psarc").delete();
            new File(Main.inpath + "data02.psarc").delete();
            new File(Main.inpath + "dlc1.psarc").delete();
            new File(Main.inpath + "dlc2.psarc").delete();

            Main.ArcTarget type;
            Main.ArcKey key;
            if (baseRad.isSelected()) {
                type = Main.ArcTarget.BASE;
                key = Main.ArcKey.BASE;
                System.out.println("Begin download of data (Game version 1.0)");
            } else
            if (patchRad.isSelected()) {
                type = Main.ArcTarget.LATEST;
                key = Main.ArcKey.LATEST;
                System.out.println("Begin download of data02 (Game version 1.04)");
            } else
            if (hdRad.isSelected()) {
                type = Main.ArcTarget.ADDON_HD;
                key = Main.ArcKey.ADDON_HD;
                System.out.println("Begin download of dlc1 (HD DLC)");
            } else
            if (furyRad.isSelected()) {
                type = Main.ArcTarget.ADDON_HD_FURY;
                key = Main.ArcKey.ADDON_HD_FURY;
                System.out.println("Begin download of dlc2 (Fury DLC)");
            } else {
                return; // fire hydrant
            }

            frame.dispose();
            invoker.setVisible(false);

            Thread downloaderPopupThread = new Thread(new Runnable() { // run on separate thread to prevent GUI freezing
                @Override
                public void run() {
                    // download file
                    boolean downloader = new Fozzie().DownloadFile(type.toString(), Main.inpath, "asset.pkg");
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
                    System.out.println("Extracting asset.pkg");
                    Process p;
                    try {
                        if (!Main.windows) {p = Runtime.getRuntime().exec(new String[]{"bash","-c","cd " + Main.inpath + ";wine pkg2zip.exe -x asset.pkg " + key.toString()});}
                        else {p = Runtime.getRuntime().exec(new String[]{Main.inpath + "pkg2zip.exe", "-x", "asset.pkg", key.toString()}, null, new File(Main.inpath));} //inpath cannot change here
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
                    } else if (type == Main.ArcTarget.LATEST) {
                        extracted = "patch/PCSA00015";
                        name = "data02.psarc";
                    } else if (type == Main.ArcTarget.ADDON_HD) {
                        extracted = "addcont/PCSA00015/DLC1W2048PACKAGE";
                        name = "dlc1.psarc";
                    } else if (type == Main.ArcTarget.ADDON_HD_FURY) {
                        extracted = "addcont/PCSA00015/DLC2W2048PACKAGE";
                        name = "dlc2.psarc";
                    } else {
                        System.out.println("Internal Error: Bert got dementia. Get a programmer!");
                        JOptionPane.showMessageDialog(invoker, "Internal Error: Bert got dementia. Get a programmer!", "Fatal Error", JOptionPane.ERROR_MESSAGE);
                        new File(Main.inpath + "asset.pkg").delete();
                        new File(Main.inpath + "app/").delete();
                        new File(Main.inpath + "patch/").delete();
                        new File(Main.inpath + "addcont/").delete();
                        invoker.setEnabled(true);
                        invoker.setVisible(true);
                        invoker.toFront();
                        invoker.repaint();
                        return;
                    }
                    try {
                        if (!Main.windows) {p = Runtime.getRuntime().exec(new String[]{"bash","-c","cd " + Main.inpath + ";wine psvpfsparser.exe -i " + extracted + " -o ./temp/ -z " + key.toString() + " -f cma.henkaku.xyz"});}
                        else {p = Runtime.getRuntime().exec(new String[]{Main.inpath + "psvpfsparser.exe", "-i", extracted, "-o", "./temp/", "-z", key.toString(), "-f", "cma.henkaku.xyz"}, null, new File(Main.inpath));}
                        p.waitFor();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        JOptionPane.showMessageDialog(invoker, "CRITICAL FAILURE: " + e.getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
                        new File(Main.inpath + "asset.pkg").delete();
                        new File(Main.inpath + "app/").delete();
                        new File(Main.inpath + "patch/").delete();
                        new File(Main.inpath + "addcont/").delete();
                        invoker.setEnabled(true);
                        invoker.setVisible(true);
                        invoker.toFront();
                        invoker.repaint();
                        return;
                    }

                    // stage & cleanup
                    System.out.println("Cleaning up");
                    new File(Main.inpath + "asset.pkg").delete();
                    new File(Main.inpath + "temp/PSP2/" + name).renameTo(new File(Main.inpath + name));
                    Main.deleteDir(new File(Main.inpath + "addcont/"));
                    Main.deleteDir(new File(Main.inpath + "patch/"));
                    Main.deleteDir(new File(Main.inpath + "app/"));
                    Main.deleteDir(new File(Main.inpath + "temp/"));

                    // restore controls
                    JOptionPane.showMessageDialog(frame, "Assets downloaded successfully.", "Download Complete", JOptionPane.INFORMATION_MESSAGE);
                    invoker.setEnabled(true);
                    invoker.setVisible(true);
                    invoker.toFront();
                    invoker.repaint();
                }
            });
            downloaderPopupThread.start();
        }
    }
}
