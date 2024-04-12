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

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class MissPiggy implements ActionListener {
    JFrame frame = new JFrame();
    JPanel frameContainer;
    JPanel actionsContainer;
    JPanel descriptionContainer;

    //JPanel menuBarContainerPanel = new JPanel();
    public JMenuBar menuBar;
    public JMenu fileMenu;
    public JMenu toolsMenu;
    public JMenu helpMenu;
    //JMenuItem menuItem;

    JScrollPane modListScrollContainer;
    public JList<String/*Main.Mod*/> modList;
    private JButton toggleButton;
    private JButton moveUpButton;
    private JButton deleteButton1;
    private JButton moveDownButton;
    private JButton optionsButton;
    private JButton importButton;
    private JButton deployButton;

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

        // populate menu bar
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        toolsMenu = new JMenu("Tools");
        helpMenu = new JMenu("Help");

        fileMenu.add(new JMenuItem("Deploy All Mods"));
        fileMenu.add(new JMenuItem("Import Mod..."));
        fileMenu.add(new JMenuItem("Remove All"));
        fileMenu.add(new JSeparator());
        fileMenu.add(new JMenuItem("Options"));
        fileMenu.add(new JMenuItem("Quit"));

        toolsMenu.add(new JMenuItem("Edit Metadata")); // disabled if a mod is not selected from the list
        toolsMenu.add(new JMenuItem("Generate New Mod from Folder..."));

        helpMenu.add(new JMenuItem("About Firestar"));

        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);
        menuBar.setVisible(true);
        frame.setJMenuBar(menuBar);

        frame.add(frameContainer); // initialize window contents -- will be handled by IntelliJ IDEA

        InitializeModListInGUI(); // present mod list

        fileMenu.getItem(0).addActionListener(this);
        fileMenu.getItem(1).addActionListener(this);
        fileMenu.getItem(2).addActionListener(this);
        fileMenu.getItem(4).addActionListener(this);
        fileMenu.getItem(5).addActionListener(this);
        toolsMenu.getItem(0).addActionListener(this);
        toolsMenu.getItem(1).addActionListener(this);
        helpMenu.getItem(0).addActionListener(this);

        // display window
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

        // todo this needs fixing before we can map it to Main.Mods and finish the other functionality around it

        // add text entry for each
        int i = 0;
        System.out.println("Initializing modList to GUI with length of" + Main.Mods.size() + "units"); //debug
        while (i < Main.Mods.size()) {
            JLabel label = new JLabel(Main.Mods.get(i).friendlyName);
            label.setVisible(true);
            modList.add(label);

            //debug
            if (Main.Mods.get(i).author == null) {Main.Mods.get(i).author = "Anonymous";}
            System.out.println("Added " + Main.Mods.get(i).friendlyName + " by " + Main.Mods.get(i).author);

            i++;
        }
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == fileMenu.getItem(5)) {System.exit(0);} else
        if (actionEvent.getSource() == fileMenu.getItem(0)) {deployModGUI();} else
        if (actionEvent.getSource() == fileMenu.getItem(1)) {importModGUI();} else
        if (actionEvent.getSource() == fileMenu.getItem(2)) {removeAllGUI();} else
        if (actionEvent.getSource() == fileMenu.getItem(4)) {optionsGUI();}
    }

    // Will likely split the below functions into separate classes to work with intellij GUI designer.

    public void deployModGUI() {
        // todo dialog that monitors psarc progress, handles file transfers, etc
        // could prevent closing program during this process? (may also be unnecessary)
    }

    public void importModGUI() {
        // todo call system shell to request file picker
    }

    public void removeAllGUI() {
        // todo warning dialog that nukes list when Yes is clicked
    }

    public void optionsGUI() {
        // todo settings page w/ reset switch
    }

    public void generatorGUI() {
        // todo mod packer
    }

    public void metaEditorGUI() {
        // todo tag editor
    }

    public void aboutGUI() {
        // todo about page
    }
}
