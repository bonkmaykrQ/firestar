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

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE;

public class Clifford implements ActionListener {
    private JFrame frame = new JFrame();
    private JPanel frameContainer;
    private JTextField fName;
    private JTextField fAuthor;
    private JTextField fVersion;
    private JTextPane fDescription;
    private JButton savebtn;
    private JButton cancelbtn;

    MissPiggy invoker;
    Main.Mod mod;
    int index;
    File directory;

    boolean creating;

    public void Action(MissPiggy inv, int modindex) { // Editor
        invoker = inv;
        mod = Main.Mods.get(modindex);
        index = modindex;
        creating = false;

        frame.add(frameContainer);
        try {
            BufferedImage windowIcon = ImageIO.read(Main.class.getResourceAsStream("/titleIcon.png"));
            frame.setIconImage(windowIcon);
        } catch (IOException e) {
            System.out.println("ERROR: Failed to find resource titleIcon.png. Window will not have an icon.");
        }
        frame.setSize(600, 300); // 1280 800
        frame.setMinimumSize(new Dimension(200,100));
        frame.setTitle("Options");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        frame.setLayout(new GridLayout());
        frame.setLocationRelativeTo(null);
        frame.setAlwaysOnTop(true);

        fName.setText(mod.friendlyName);
        fAuthor.setText(mod.author);
        fVersion.setText(String.valueOf(mod.version));
        fDescription.setText(mod.description);

        cancelbtn.addActionListener(this);
        savebtn.addActionListener(this);

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

    public void Action(MissPiggy inv, File dir) { // Generator
        invoker = inv;
        directory = dir;
        creating = true;

        frame.add(frameContainer);
        frame.setSize(600, 200); // 1280 800
        frame.setMinimumSize(new Dimension(200,100));
        frame.setTitle("Options");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        frame.setLayout(new GridLayout());
        frame.setLocationRelativeTo(null);
        frame.setAlwaysOnTop(true);

        cancelbtn.addActionListener(this);
        savebtn.addActionListener(this);

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
        } else if (actionEvent.getSource() == savebtn && !creating) {
            try {
                mod.version = Integer.parseInt(fVersion.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Mod version must be a valid integer.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            mod.friendlyName = fName.getText();
            mod.author = fAuthor.getText();
            mod.description = fDescription.getText();

            JSONObject container = new JSONObject();
            if (mod.friendlyName.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Mod name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            container.put("version", mod.version);
            container.put("friendlyName", mod.friendlyName);
            container.put("author", mod.author);
            container.put("description", mod.description);
            container.put("loaderversion", mod.loaderversion);
            container.put("game", mod.game);

            try {
                new ZipFile(System.getProperty("user.home") + "/.firestar/mods/" + mod.path.trim()).setComment(container.toString());
            } catch (ZipException e) {
                System.out.println(e.getMessage());
                JOptionPane.showMessageDialog(frame, "An error has occured.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }

            Main.Mods.set(index, mod);
            invoker.frame.setEnabled(true);
            invoker.InitializeModListInGUI();
            frame.dispose();
        } else if (actionEvent.getSource() == savebtn && creating) {
            if (fName.getText().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Mod name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                Integer.parseInt(fVersion.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Mod version must be a valid integer.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Firestar Mod Package", "fstar"));
            if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                ZipFile zip = new ZipFile(fileChooser.getSelectedFile());
                try {
                    zip.addFolder(new File(directory.getAbsolutePath() + "/data"));
                    if (new File(directory.getAbsolutePath() + "/delete.txt").exists()) {
                        zip.addFile(new File(directory.getAbsolutePath() + "/delete.txt"));
                    }
                    if (new File(directory.getAbsolutePath() + "/pack.png").exists()) {
                        zip.addFile(new File(directory.getAbsolutePath() + "/pack.png"));
                    }

                    JSONObject container = new JSONObject();
                    container.put("version", Integer.parseInt(fVersion.getText()));
                    container.put("friendlyName", fName.getText());
                    container.put("author", fAuthor.getText());
                    container.put("description", fDescription.getText());
                    container.put("loaderversion", 0); // TODO for later versions: Change depending on features inside of mod folder
                    container.put("game", "2048");

                    zip.setComment(container.toString());
                    zip.close();
                } catch (Exception e) {
                    fileChooser.getSelectedFile().delete(); //cleanup
                    System.out.println(e.getMessage());
                    JOptionPane.showMessageDialog(frame, "An error has occured.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                JOptionPane.showMessageDialog(frame, "Mod file created", "Success", JOptionPane.INFORMATION_MESSAGE);
                invoker.frame.setEnabled(true);
                frame.dispose();
            }
        }
    }
}
