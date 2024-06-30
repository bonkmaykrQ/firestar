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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

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

    public void Action(MissPiggy inv, int modindex) {
        invoker = inv;
        mod = Main.Mods.get(modindex);
        index = modindex;

        frame.add(frameContainer);
        frame.setSize(600, 200); // 1280 800
        frame.setMinimumSize(new Dimension(200,100));
        frame.setTitle("Options");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
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

    public void Action(MissPiggy inv, File dir) {

    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == cancelbtn) {
            invoker.frame.setEnabled(true);
            frame.dispose();
        } else
        if (actionEvent.getSource() == savebtn) {
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
        }
    }
}
