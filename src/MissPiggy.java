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
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Pattern;
import net.lingala.zip4j.*;
import net.lingala.zip4j.exception.ZipException;
import org.json.JSONObject;
import static java.nio.file.StandardCopyOption.*;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class MissPiggy implements ActionListener {
    BufferedImage windowIcon;
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
    public JList<String> modList;
    private JButton toggleButton;
    private JButton moveUpButton;
    private JButton deleteButton1;
    private JButton moveDownButton;
    private JButton optionsButton;
    private JButton importButton;
    private JButton deployButton;
    private JTextPane descriptionField;

    //private int selectedItem;

    public String priorityList;

    // Initialize the main window
    public void Action(/*Main entryPoint*/) {
        System.out.println("Main window created");
        System.out.println("Loading program configuration");
        Main.loadConf(this);

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
        toolsMenu.add(new JMenuItem("Create Soundtrack Mod..."));
        //toolsMenu.add(new JMenuItem("Download Mod from URL")); // TODO: implement. move option to File menu. should be ez

        helpMenu.add(new JMenuItem("About Firestar"));

        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);
        menuBar.setVisible(true);
        frame.setJMenuBar(menuBar);

        frame.add(frameContainer); // initialize window contents -- will be handled by IntelliJ IDEA

        InitializeModListStructure();
        InitializeModListInGUI(); // present mod list

        fileMenu.getItem(0).addActionListener(this);
        fileMenu.getItem(1).addActionListener(this);
        fileMenu.getItem(2).addActionListener(this);
        fileMenu.getItem(4).addActionListener(this);
        fileMenu.getItem(5).addActionListener(this);
        toolsMenu.getItem(0).addActionListener(this);
        toolsMenu.getItem(1).addActionListener(this);
        helpMenu.getItem(0).addActionListener(this);

        deployButton.addActionListener(this);
        importButton.addActionListener(this);
        deleteButton1.addActionListener(this);
        optionsButton.addActionListener(this);
        moveUpButton.addActionListener(this);
        moveDownButton.addActionListener(this);
        toggleButton.addActionListener(this);

        descriptionField.getDocument().putProperty("filterNewlines", Boolean.FALSE);
        modList.addListSelectionListener(e -> {
            String authorDisplay;
            File pathReference = new File(System.getProperty("user.home") + "/.firestar/mods/" + Main.Mods.get(modList.getSelectedIndex()).path);
            DecimalFormat df = new DecimalFormat("##.##");
            df.setRoundingMode(RoundingMode.UP);
            float modFileSize = pathReference.length(); //precise units
            String modFileSizeStr = String.valueOf(modFileSize);
            String modFileSizeUnits = "bytes"; //todo: don't show decimals for bytes
            if (pathReference.length() >= 1024) {
                modFileSizeStr = String.valueOf(df.format(modFileSize / 1024));
                modFileSizeUnits = "Kilobytes";
            }
            if (pathReference.length() >= 1024 * 1024) {
                modFileSizeStr = String.valueOf(df.format(modFileSize / (1024 * 1024)));
                modFileSizeUnits = "Megabytes";
            }
            if (pathReference.length() >= 1024 * 1024 * 1024) {
                modFileSizeStr = String.valueOf(df.format(modFileSize / (1024 * 1024 * 1024)));
                modFileSizeUnits = "Gigabytes";
            }
            if (Main.Mods.get(modList.getSelectedIndex()).author == null) {
                authorDisplay = "an Unknown Author";
            } else {
                authorDisplay = Main.Mods.get(modList.getSelectedIndex()).author;
            }
            descriptionField.setText(
                "\"" + Main.Mods.get(modList.getSelectedIndex()).friendlyName + "\"\n" +
                "by " + authorDisplay + "\n\n" +
                "Version " + Main.Mods.get(modList.getSelectedIndex()).version + "\n" +
                        modFileSizeStr + " " + modFileSizeUnits + " in size" +
                "\n\n" + Main.Mods.get(modList.getSelectedIndex()).description
        );});

        // display window
        try {
            windowIcon = ImageIO.read(new File(System.getProperty("user.dir") + "/resources/titleIcon.png"));
            frame.setIconImage(windowIcon);
        } catch (IOException e) {
            System.out.println("ERROR: Failed to find /resources/titleIcon.png. Window will not have an icon.");
        }
        frame.setSize(800, 600); // 1280 800
        frame.setMinimumSize(new Dimension(640,480));
        frame.setTitle("Firestar Mod Manager");
        frame.setResizable(true);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void InitializeModListStructure() {
        // cleanup
        Main.Mods.clear();

        // get current list of mods from file
        // todo: rewrite when modpacks/playlists are added
        try {
            priorityList = new String(Files.readAllBytes(Paths.get(System.getProperty("user.home") + "/.firestar/mods/index")));
        } catch (IOException e) {
            File priorityListFileHandle = new File(System.getProperty("user.home") + "/.firestar/mods/index");
            new File(System.getProperty("user.home") + "/.firestar/mods/").mkdirs();
            if(!priorityListFileHandle.isFile()){
                try {
                    priorityListFileHandle.createNewFile();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            priorityList = "";
        }

        // initialize data structures from each list entry
        String[] pListArray = priorityList.split("\n");
        Arrays.sort(pListArray);
        System.out.println("Initializing modList from file with length of " + pListArray.length + " units"); //debug
        for (String s : pListArray) {
            /*
            Do nothing if the index number is not valid.
            there probably is not a practical reason to do this, but I want to eliminate any undefined behaviors while we're here.
            we'll also eliminate any syntax errors caused by the lack of a = sign

            06/29/24 - also skip files that were manually removed but remain in the list
            */
            if (s.split("=")[0].matches("[0-9]+=*") &&
                    new File(System.getProperty("user.home") + "/.firestar/mods/" + s.substring(s.indexOf("=") + 1)).exists()) {
                //append mod to list from substring
                Main.Mod m = new Main().new Mod();
                m.path = s.substring(s.indexOf("=") + 1);
                System.out.println("found file " + m.path);

                //get json metadata from zip comment
                JSONObject metadata;
                try {
                    metadata = new JSONObject(new ZipFile(System.getProperty("user.home") + "/.firestar/mods/" + m.path).getComment());
                    if (metadata.has("friendlyName")) {m.friendlyName = metadata.get("friendlyName").toString();} else {m.friendlyName = m.path;}
                    if (metadata.has("description")) {m.description = metadata.get("description").toString();}
                    if (metadata.has("version")) {m.version = Integer.parseInt(metadata.get("version").toString());}
                    if (metadata.has("author")) {m.author = metadata.get("author").toString();}
                    if (metadata.has("loaderversion")) {m.loaderversion = Integer.parseInt(metadata.get("loaderversion").toString());}
                    if (metadata.has("game")) {m.game = metadata.get("game").toString();}

                    //send to list
                    Main.Mods.add(m);
                } catch (Exception e) {
                    System.out.println("WARNING: mod entry for " + s + " was found but does not contain valid JSON metadata. skipping");
                }
            } else {
                if (!s.isEmpty()) {System.out.println("WARNING: mod entry for " + s + " doesn't actually exist. skipping");}
            }
        }
    }

    public void InitializeModListInGUI() { // i really wanted this to be "lights, camera, action" but the code organizing kept getting stupider and stupider so i gave up
        // cleanup
        descriptionField.setText("Select a mod from the list on the right to view more details, or to make changes to your installation.");
        modList.clearSelection();
        modList.removeAll();
        modList.setVisibleRowCount(Main.Mods.size());
        modList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // add text entry for each
        int i = 0;
        /*JLabel[]*/String[] contents = new String[Main.Mods.size()];
        System.out.println("Initializing modList to GUI with length of " + Main.Mods.size() + " units"); //debug
        while (i < Main.Mods.size()) {
            contents[i] = Main.Mods.get(i).friendlyName;

            //debug
            String authorDisplay;
            if (Main.Mods.get(i).author == null) {authorDisplay = "Anonymous";} else {authorDisplay = "\"" + Main.Mods.get(i).author + "\"";}
            System.out.println("Added " + Main.Mods.get(i).friendlyName + " by " + authorDisplay);

            i++;
        }
        modList.setListData(contents);
    }

    private ListSelectionListener whenItemSelected() {
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == fileMenu.getItem(5)) {System.exit(0);} else
        if (actionEvent.getSource() == fileMenu.getItem(0)) {deployModGUI();} else
        if (actionEvent.getSource() == deployButton) {deployModGUI();} else
        if (actionEvent.getSource() == importButton) {importModGUI();} else
        if (actionEvent.getSource() == fileMenu.getItem(1)) {importModGUI();} else
        if (actionEvent.getSource() == fileMenu.getItem(2)) {removeAllGUI();} else
        if (actionEvent.getSource() == optionsButton) {optionsGUI();} else
        if (actionEvent.getSource() == fileMenu.getItem(4)) {optionsGUI();} else

        if (actionEvent.getSource() == moveUpButton) {throwUnimplemented();} else // todo
        if (actionEvent.getSource() == moveDownButton) {throwUnimplemented();} else // todo
        if (actionEvent.getSource() == toggleButton) {throwUnimplemented();} else // todo
        if (actionEvent.getSource() == deleteButton1) {deleteSelected();} else // todo

        if (actionEvent.getSource() == helpMenu.getItem(0)) {new Rowlf().displayAboutScreen();}
    }

    // Will likely split the below functions into separate classes to work with intellij GUI designer.

    public void deployModGUI() {
        // prevent interruptions
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setEnabled(false);

        // start
        new Gonzo().DeployMods(this);
    }

    public void wrapUpDeployment() {
        // restore functionality to main window
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setEnabled(true);
    }

    public void importModGUI() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        //fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("All", "zip", "agr", "agrc", "agrf", "fstar"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("ZIP Archive", "zip"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Firestar Mod Package", "agr", "agrc", "agrf", "fstar")); //what about fstar?

        int result = fileChooser.showOpenDialog(frame);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            System.out.println("Importing selected mod file \"" + selectedFile.getName() + "\"");

            ZipFile zipImporterHandler = new ZipFile(selectedFile.getPath());
            if (zipImporterHandler.isValidZipFile()) {
                try {
                    Path importDestination = Paths.get(System.getProperty("user.home") + "/.firestar/mods/"
                            + selectedFile.getName() + "_" + Main.Mods.size() + ".zip");
                    Files.copy(Paths.get(selectedFile.getPath()), importDestination, StandardCopyOption.REPLACE_EXISTING);
                    String importDestinationName = importDestination.toFile().getName();

                    BufferedWriter bw = new BufferedWriter(new FileWriter(System.getProperty("user.home") + "/.firestar/mods/index", true));
                    bw.write(Main.Mods.size() + "=" + importDestinationName);
                    bw.newLine();
                    bw.close();

                    InitializeModListStructure();
                    InitializeModListInGUI();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    JOptionPane.showMessageDialog(frame, "An error has occured.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                System.out.println("ERROR: File is not a valid ZIP archive. Aborting.");
                JOptionPane.showMessageDialog(frame, "Whoops, that's not a valid ZIP archive.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void removeAllGUI() {
        // todo warning dialog that nukes list when Yes is clicked
        int result = JOptionPane.showConfirmDialog(frame, "Do you really want to delete all mods?", "Remove All", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            for (Main.Mod entry : Main.Mods) {
                new File(System.getProperty("user.home") + "/.firestar/mods/" + entry.path).delete();
            }
        }
        new File(System.getProperty("user.home") + "/.firestar/mods/index").delete();
        Main.Mods.clear();

        InitializeModListStructure();
        InitializeModListInGUI();
    }

    public void optionsGUI() {
        // todo settings page w/ reset switch
        throwUnimplemented();
    }

    public void deleteSelected() {
        File file = new File(System.getProperty("user.home") + "/.firestar/mods/" + Main.Mods.get(modList.getSelectedIndex()).path);    //new File(Main.Mods.get(modList.getSelectedIndex()).path);
        //String filename = Paths.get(Main.Mods.get(modList.getSelectedIndex()).path).getFileName().toString();                                   //redundant since Mod path is relative to /.firestar/mods

        //JOptionPane.showMessageDialog(frame, filename, "Unimplemented", JOptionPane.INFORMATION_MESSAGE); //debug

        file.delete(); System.out.println("Deleted " + Main.Mods.get(modList.getSelectedIndex()).friendlyName); //debug
        try {
            priorityList = new String(Files.readAllBytes(Paths.get(System.getProperty("user.home") + "/.firestar/mods/index")));
            String[] pListArray = priorityList.split("\n");
            Arrays.sort(pListArray);
            System.out.println("Searching modlist to remove " + Main.Mods.get(modList.getSelectedIndex()).friendlyName); //debug

            int i = 0;
            for (String s : pListArray) {
                if (file.getName().equals(s.substring(s.indexOf("=") + 1))) {
                    pListArray[i] = "";
                }
                i++;
            }


        } catch (IOException e) {
            System.out.println(e.getMessage());
            JOptionPane.showMessageDialog(frame, "An error has occured.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void generatorGUI() {
        // todo mod packer
        throwUnimplemented();
    }

    public void metaEditorGUI() {
        // todo tag editor
        throwUnimplemented();
    }

    public void throwUnimplemented() {
        JOptionPane.showMessageDialog(frame, "Unimplemented.\nSee README at https://git.worlio.com/bonkmaykr/firestar", "Unimplemented", JOptionPane.INFORMATION_MESSAGE);
    }
}
