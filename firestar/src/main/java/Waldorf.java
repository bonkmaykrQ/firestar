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
import java.io.File;
import java.io.IOException;

import static javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE;

public class Waldorf implements ActionListener {
    private JFrame frame = new JFrame();
    private JPanel frameContainer;
    private JButton okbtn;
    private JButton cancelbtn;
    private JTextField fOutpath;
    private JButton resetbtn;
    private JButton bOpenFolder;
    private JButton dwnSDKbtn;
    private JButton dwnARCbtn;
    MissPiggy invoker;

    public void Action(MissPiggy inv) {
        invoker = inv;

        frame.add(frameContainer);
        try {
            BufferedImage windowIcon = ImageIO.read(Main.class.getResourceAsStream("/titleIcon.png"));
            frame.setIconImage(windowIcon);
        } catch (IOException e) {
            System.out.println("ERROR: Failed to find /resources/titleIcon.png. Window will not have an icon.");
        }
        frame.setSize(600, 200); // 1280 800
        frame.setMinimumSize(new Dimension(200,100));
        frame.setTitle("Options");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        frame.setLayout(new GridLayout());
        frame.setLocationRelativeTo(null);
        frame.setAlwaysOnTop(true);

        cancelbtn.addActionListener(this);
        okbtn.addActionListener(this);
        resetbtn.addActionListener(this);
        bOpenFolder.addActionListener(this);

        dwnARCbtn.addActionListener(this);
        dwnSDKbtn.addActionListener(this);

        fOutpath.setText(Main.outpath);

        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e)
            {
                invoker.frame.setEnabled(true);
                e.getWindow().dispose();
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == cancelbtn) {
            invoker.frame.setEnabled(true);
            frame.dispose();
        } else
        if (actionEvent.getSource() == okbtn) {
            Main.outpath = fOutpath.getText();
            Main.writeConf();
            Main.loadConf();

            invoker.frame.setEnabled(true);
            frame.dispose();
        } else
        if (actionEvent.getSource() == resetbtn) { // todo: delete firesdk
            int result = JOptionPane.showConfirmDialog(frame,"Are you sure you want to redo the initial setup?", "Restore Default Settings", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                new File(System.getProperty("user.home") + "/.firestar/firestar.conf").delete();
                int result2 = JOptionPane.showConfirmDialog(frame,"Firestar will now close.", "Restore Default Settings", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (result2 == JOptionPane.OK_OPTION) {
                    System.exit(0);
                }
            }
        } else
        if (actionEvent.getSource() == bOpenFolder) {
            try {
                Desktop.getDesktop().open(new File(Main.inpath));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else
        if (actionEvent.getSource() == dwnARCbtn) {
            new Bert(invoker.frame);
            frame.dispose();
        } else
        if (actionEvent.getSource() == dwnSDKbtn) {
            Thread downloaderPopupThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Main.downloadDependenciesBeforeSetVisible(invoker.frame);
                    invoker.frame.setEnabled(true);
                }
            });
            downloaderPopupThread.start();
            frame.dispose();
        }
    }
}
