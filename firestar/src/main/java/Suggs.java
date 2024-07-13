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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Suggs implements ActionListener, ListSelectionListener {
    private BufferedImage windowIcon;
    public JFrame frame = new JFrame();
    private JPanel frameContainer;
    private JList dSongList;
    private JButton moveDownBtn;
    private JButton saveBtn;
    private JButton cancelBtn;
    private JTextField fTitle;
    private JTextField fArtist;
    private JLabel dTrackNo;
    private JLabel dFileSize;
    private JButton frontendDemoChooseBtn;
    private JButton frontendMainChooseBtn;
    private JButton deleteSongBtn;
    private JButton addSongBtn;
    private JButton moveUpBtn;

    public Suggs(JFrame parent) {
        parent.setEnabled(false);

        try {
            windowIcon = ImageIO.read(Main.class.getResourceAsStream("/titleIcon.png"));
            frame.setIconImage(windowIcon);
        } catch (IOException e) {
            System.out.println("ERROR: Failed to find /resources/titleIcon.png. Window will not have an icon.");
        }

        frame.add(frameContainer); // initialize window contents -- will be handled by IntelliJ IDEA

        frame.setSize(700, 400);
        frame.setMinimumSize(new Dimension(650,280));
        frame.setTitle("Soundtrack Mod Generator");
        frame.setResizable(true);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setLayout(new GridLayout());
        frame.setLocationRelativeTo(parent);
        frame.setAlwaysOnTop(true);

        cancelBtn.addActionListener(this); // TODO: put warning dialog "Are you sure? All unsaved changes will be lost."
        saveBtn.addActionListener(this);
        addSongBtn.addActionListener(this);     // file picker
        deleteSongBtn.addActionListener(this);  // delete from list
        moveUpBtn.addActionListener(this);
        moveDownBtn.addActionListener(this);
        fTitle.addActionListener(this);         // automatically change selected item when changed &
        fArtist.addActionListener(this);        // also update field when new item selected
        frontendMainChooseBtn.addActionListener(this);      // file picker for singleplayer campaign grid music
        frontendDemoChooseBtn.addActionListener(this);      // file picker for multiplayer lobby music
        dSongList.addListSelectionListener(this);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e)
            {// TODO: put warning dialog "Are you sure? All unsaved changes will be lost."
                parent.setEnabled(true);
                e.getWindow().dispose();
            }
        });

        frame.setVisible(true);
    }

    private void haveSeggs() { // kill yourself

    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {

    }

    @Override
    public void valueChanged(ListSelectionEvent listSelectionEvent) { // TODO: change fields on form, show file size, and show MT_(track number) when selection changed.

    }
}
