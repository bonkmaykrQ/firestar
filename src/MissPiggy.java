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
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class MissPiggy {
    JFrame frame = new JFrame();
    JPanel frameContainer;
    JPanel actionsContainer;
    JPanel descriptionContainer;

    JMenuBar menuBar;
    JMenu menu;
    JMenuItem menuItem;
    JScrollPane modListScrollContainer;
    JList<String/*Main.Mod*/> modList;
    private JButton toggleButton;
    private JButton moveUpButton;
    private JButton deleteButton1;
    private JButton moveDownButton;
    private JButton optionsButton;
    private JButton importButton;
    private JButton generateButton;

    private int selectedItem;

    // Initialize the main window
    public void Action(/*Main entryPoint*/) {
        // todo construct contents
        // todo display modlist

        /// DEBUG ///
        Main.Mod testModEntry = /*entryPoint*/new Main().new Mod(); //this is retarded? we're making a new object of a certain type, why the fuck do you care where it comes from? static or regardless??
        testModEntry.friendlyName = "Example Mod";
        testModEntry.game = "2048";
        testModEntry.path = "unused";
        testModEntry.version = 1;
        testModEntry.priority = 0; //might discard this in favor of the list index for simplicity
        testModEntry.loaderversion = 0;
        Main.Mods.add(testModEntry);
        Main.Mods.add(testModEntry);
        Main.Mods.add(testModEntry);
        ///-/////-///

        InitializeModListInGUI();

        frame.add(frameContainer); // initialize window contents -- will be handled by IntelliJ IDEA

        frame.setSize(800, 600); // 1280 800
        frame.setTitle("Firestar Mod Manager");
        frame.setResizable(true);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout());
        frame.setVisible(true);
    }

    public void InitializeModListInGUI() { // i really wanted this to be "lights, camera, action" but the code organizing kept getting stupider and stupider so i gave up
        // cleanup
        modList.clearSelection();
        modList.removeAll();

        // add text entry for each
        int i = 0;
        System.out.println("Initializing modList to GUI with length of" + Main.Mods.size() + "units"); //debug
        while (i < Main.Mods.size()) {
            modList.add(new JLabel(Main.Mods.get(i).friendlyName));

            //debug
            if (Main.Mods.get(i).author == null) {Main.Mods.get(i).author = "Anonymous";}
            System.out.println("Added " + Main.Mods.get(i).friendlyName + " by " + Main.Mods.get(i).author);

            i++;
        }
    }
}
