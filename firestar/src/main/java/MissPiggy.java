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
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import net.lingala.zip4j.*;
import net.lingala.zip4j.exception.ZipException;
import org.json.JSONException;
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
    private JScrollPane descriptionScroller;

    //private int selectedItem;

    public String priorityList;
    public String blackList;

    public boolean listenersAlreadySet = false; // was written to troubleshoot a bug but this wasn't actually the cause

    public void Action() {
        Action(false);
    }

    // Initialize the main window
    public void Action(boolean forceNag) {
        System.out.println("Main window created");
        System.out.println("Loading program configuration");
        Main.loadConf(this);

        // populate menu bar
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        toolsMenu = new JMenu("Tools");
        helpMenu = new JMenu("Help");

        fileMenu.add(new JMenuItem("Deploy Mods"));
        fileMenu.add(new JMenuItem("Import Mod from File"));
        //fileMenu.add(new JSeparator());
        fileMenu.add(new JMenuItem("Delete All"));
        fileMenu.add(new JSeparator());
        fileMenu.add(new JMenuItem("Options"));
        fileMenu.add(new JMenuItem("Quit"));

        toolsMenu.add(new JMenuItem("Edit Metadata")); // disabled if a mod is not selected from the list
        toolsMenu.add(new JMenuItem("Generate New Mod from Folder..."));
        toolsMenu.add(new JMenuItem("Create Soundtrack Mod..."));
        //toolsMenu.add(new JMenuItem("Download Mod from URL")); // TODO: implement. move option to File menu. should be ez

        helpMenu.add(new JMenuItem("Manual"));
        helpMenu.add(new JSeparator());
        helpMenu.add(new JMenuItem("Source Code")); //replace with Website 'screwgravity.net' and 'Issue Tracker' gitea later
        helpMenu.add(new JMenuItem("License"));
        helpMenu.add(new JSeparator());
	helpMenu.add(new JMenuItem("Check for Updates"));
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
        toolsMenu.getItem(2).addActionListener(this);
        helpMenu.getItem(0).addActionListener(this);
        helpMenu.getItem(2).addActionListener(this);
        helpMenu.getItem(3).addActionListener(this);
        helpMenu.getItem(5).addActionListener(this);
	    helpMenu.getItem(6).addActionListener(this);

        deployButton.addActionListener(this);
        importButton.addActionListener(this);
        deleteButton1.addActionListener(this);
        optionsButton.addActionListener(this);
        moveUpButton.addActionListener(this);
        moveDownButton.addActionListener(this);
        toggleButton.addActionListener(this);

        descriptionField.getDocument().putProperty("filterNewlines", Boolean.FALSE);

        // display window
        try {
            windowIcon = ImageIO.read(Main.class.getResourceAsStream("/titleIcon.png"));
            frame.setIconImage(windowIcon);
        } catch (IOException e) {
            System.out.println("ERROR: Failed to find /resources/titleIcon.png. Window will not have an icon.");
        }

        menuBar.setBackground(new Color(25, 41, 93));
        fileMenu.setForeground(new Color(255, 255, 255));
        fileMenu.setFont(Main.fExo2.deriveFont(Font.BOLD).deriveFont(12f));
        toolsMenu.setForeground(new Color(255, 255, 255));
        toolsMenu.setFont(Main.fExo2.deriveFont(Font.BOLD).deriveFont(12f));
        helpMenu.setForeground(new Color(255, 255, 255));
        helpMenu.setFont(Main.fExo2.deriveFont(Font.BOLD).deriveFont(12f));

        toggleButton.setFont(Main.fExo2.deriveFont(Font.BOLD).deriveFont(12f));
        deleteButton1.setFont(Main.fExo2.deriveFont(Font.BOLD).deriveFont(12f));
        moveDownButton.setFont(Main.fExo2.deriveFont(Font.BOLD).deriveFont(12f));
        moveUpButton.setFont(Main.fExo2.deriveFont(Font.BOLD).deriveFont(12f));
        optionsButton.setFont(Main.fExo2.deriveFont(Font.BOLD).deriveFont(12f));
        importButton.setFont(Main.fExo2.deriveFont(Font.BOLD).deriveFont(12f));
        deployButton.setFont(Main.fExo2.deriveFont(Font.BOLD).deriveFont(12f));

        ((DefaultCaret)descriptionField.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE); // prevent automatically scrolling to the bottom when mod details exceed window size

        frame.setSize(800, 600); // 1280 800
        frame.setMinimumSize(new Dimension(640,480));
        frame.setTitle("Firestar Mod Manager");
        frame.setResizable(true);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout());
        frame.setLocationRelativeTo(null);
	
	if (Main.checkUpdates) { StartErnie(); }

        frame.setVisible(true);
    }

    public void InitializeModListStructure() {
        // cleanup
        Main.Mods.clear();

        // get current list of mods from file
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

            File mod = new File(System.getProperty("user.home") + "/.firestar/mods/" + s.substring(s.indexOf("=") + 1).trim());

            if (s.split("=")[0].matches("[0-9]+=*") &&
                    mod.exists()) {
                //append mod to list from substring
                Main.Mod m = new Main().new Mod();
                m.path = s.substring(s.indexOf("=") + 1).trim();
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
                    System.out.println(e.getMessage());
                }
            } else {
                if (!s.isEmpty()) {System.out.println("WARNING: mod entry for " + s + " doesn't actually exist. skipping");}
            }
        }

        try {
            blackList = new String(Files.readAllBytes(Paths.get(System.getProperty("user.home") + "/.firestar/mods/blacklist")));
        } catch (IOException e) {
            File blackListFileHandle = new File(System.getProperty("user.home") + "/.firestar/mods/blacklist");
            new File(System.getProperty("user.home") + "/.firestar/mods/").mkdirs();
            if(!blackListFileHandle.isFile()){
                try {
                    blackListFileHandle.createNewFile();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            blackList = "";
        }

        // initialize data structures from each list entry
        String[] bListArray = blackList.split("\n");
        //Arrays.sort(bListArray);
        System.out.println("Initializing blacklist from file with length of " + bListArray.length + " units"); //debug
        for (String s : bListArray) {
            for (Main.Mod m : Main.Mods) {
                if (s.trim().equals(m.path)) {
                    m.enabled = false;
                }
            }
        }
    }

    public void InitializeModListInGUI() {
        // cleanup
        if (listenersAlreadySet) {modList.removeListSelectionListener(modList.getListSelectionListeners()[0]);} // was written to troubleshoot a bug but this wasn't actually the cause
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
            if (Main.Mods.get(i).friendlyName == null || Main.Mods.get(i).friendlyName.isEmpty())
            {Main.Mods.get(i).friendlyName = Main.Mods.get(i).path;}
            if (Main.Mods.get(i).enabled) {contents[i] = Main.Mods.get(i).friendlyName;}
            else {contents[i] = Main.Mods.get(i).friendlyName + " (Disabled)";}

            //debug
            String authorDisplay;
            if (Main.Mods.get(i).author == null || Main.Mods.get(i).author.isEmpty()) {authorDisplay = "Anonymous";} else {authorDisplay = "\"" + Main.Mods.get(i).author + "\"";}
            System.out.println("Added " + Main.Mods.get(i).friendlyName + " by " + authorDisplay);

            i++;
        }
        modList.setListData(contents);
        createSelectionEventListener();
    }
    
    private void StartErnie() {
	new Thread(new Runnable() {
            @Override
            public void run() {
                new Ernie(frame); // changed away from runnable in order to pass params to constructor -bonk
            }
        }).start();
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

        if (actionEvent.getSource() == moveUpButton) {moveUp(modList.getSelectedIndex());} else
        if (actionEvent.getSource() == moveDownButton) {moveDown(modList.getSelectedIndex());} else

        if (actionEvent.getSource() == toggleButton) {toggleSelected(modList.getSelectedIndex());} else
        if (actionEvent.getSource() == deleteButton1) {deleteSelected();} else

        if (actionEvent.getSource() == toolsMenu.getItem(0)) {metaEditorGUI(modList.getSelectedIndex());} else
        if (actionEvent.getSource() == toolsMenu.getItem(1)) {generatorGUI();} else
        if (actionEvent.getSource() == toolsMenu.getItem(2)) {new Suggs(frame);} else

        if (actionEvent.getSource() == helpMenu.getItem(0)) {
            try {
                Desktop.getDesktop().browse(new URI("https://git.worlio.com/bonkmaykr/firestar/wiki/"));
            } catch (Exception e) {
                System.out.println(e.getMessage());
                JOptionPane.showMessageDialog(frame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else
        if (actionEvent.getSource() == helpMenu.getItem(2)) {
            try {
                Desktop.getDesktop().browse(new URI("https://git.worlio.com/bonkmaykr/firestar"));
            } catch (Exception e) {
                System.out.println(e.getMessage());
                JOptionPane.showMessageDialog(frame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else
        if (actionEvent.getSource() == helpMenu.getItem(3)) {
            try {
                Desktop.getDesktop().browse(new URI("https://www.gnu.org/licenses/gpl-3.0.en.html"));
            } catch (Exception e) {
                System.out.println(e.getMessage());
                JOptionPane.showMessageDialog(frame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else
        if (actionEvent.getSource() == helpMenu.getItem(5)) {StartErnie();} else
        if (actionEvent.getSource() == helpMenu.getItem(6)) {new Rowlf(frame);}
    }

    // Will likely split the below functions into separate classes to work with intellij GUI designer.

    public void deployModGUI() {
        int i = 0;
        for (Main.Mod m : Main.Mods) {
            if (m.enabled) {i++;}
        }

        if (i > 0) {
            int result = JOptionPane.showConfirmDialog(frame, "A new PSARC will be generated. This can take several minutes.\nDuring this time, your computer may be very busy or slow.\n\nIt will be placed in: " + Main.outpath + "\n\nAre you sure you want to continue?", "Deploy Mods", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                // prevent interruptions
                frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                frame.setEnabled(false);

                if (!new File(Main.inpath + "psp2psarc.exe").exists()) {
                    JOptionPane.showMessageDialog(frame, "psp2psarc is missing.\nPlease select \"Get Dependencies\" in the Options menu.", "Error", JOptionPane.ERROR_MESSAGE);
                    wrapUpDeployment();
                    return;
                }

                if (!new File(Main.inpath + "data.psarc").exists() &&
                        !new File(Main.inpath + "data1.psarc").exists() &&
                        !new File(Main.inpath + "data2.psarc").exists() &&
                        !new File(Main.inpath + "dlc1.psarc").exists() &&
                        !new File(Main.inpath + "dlc2.psarc").exists()) {
                    JOptionPane.showMessageDialog(frame, "You have no PSARCs.\nPlease dump your copy of WipEout 2048 or download a PSARC from the Options menu.", "Error", JOptionPane.ERROR_MESSAGE);
                    wrapUpDeployment();
                    return;
                }

                // start
                new Gonzo().DeployMods(this);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Please add at least one mod file to continue.", "Error", JOptionPane.ERROR_MESSAGE);
        }
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
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Firestar Mod Package", "fstar"));

        int result = fileChooser.showOpenDialog(frame);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            System.out.println("Importing selected mod file \"" + selectedFile.getName() + "\"");

            ZipFile zipImporterHandler = new ZipFile(selectedFile.getAbsolutePath());
            if (zipImporterHandler.isValidZipFile()) {
                try {
                    JSONObject json = new JSONObject(new ZipFile(selectedFile.getAbsolutePath()).getComment()); // intentionally trigger exception if file is random BS
                    if ((int)json.get("loaderversion") <= Main.vint) {
                        int min=0, max=9;
                        int rand_int = (int)(Math.random()*((max-min)+1))+min;
                        int rand_int2 = (int)(Math.random()*((max-min)+1))+min;
                        int rand_int3 = (int)(Math.random()*((max-min)+1))+min;
                        Path importDestination = Paths.get(System.getProperty("user.home") + "/.firestar/mods/"
                            + selectedFile.getName() + "_" + rand_int + rand_int2 + rand_int3 + System.currentTimeMillis() + ".zip");
                        Files.copy(Paths.get(selectedFile.getPath()), importDestination, StandardCopyOption.REPLACE_EXISTING);
                        String importDestinationName = importDestination.toFile().getName();

                        BufferedWriter bw = new BufferedWriter(new FileWriter(System.getProperty("user.home") + "/.firestar/mods/index", true));
                        bw.write(Main.Mods.size() + "=" + importDestinationName);
                        bw.newLine();
                        bw.close();

                        InitializeModListStructure();
                        InitializeModListInGUI();
                    } else {
                        System.out.println("ERROR: This mod requires feature level " + json.get("loaderversion").toString() + ", but you have level " + Main.vint + ".");
                        JOptionPane.showMessageDialog(frame, "This mod requires feature level " + json.get("loaderversion").toString() + ", but you have level " + Main.vint + ".\nPlease update Firestar to the latest version.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (JSONException e) {
                    System.out.println("ERROR: File is not a valid ZIP archive with mod data. Aborting.");
                    JOptionPane.showMessageDialog(frame, "Whoops, that's not a valid mod file.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    JOptionPane.showMessageDialog(frame, "An error has occured.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                System.out.println("ERROR: File is not a valid ZIP archive with mod data. Aborting.");
                JOptionPane.showMessageDialog(frame, "Whoops, that's not a valid mod file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void removeAllGUI() {
        // todo warning dialog that nukes list when Yes is clicked
        int result = JOptionPane.showConfirmDialog(frame, "Do you really want to delete all mods?", "Remove All", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            Main.deleteDir(new File(System.getProperty("user.home") + "/.firestar/mods/"));
            Main.Mods.clear();

            InitializeModListStructure();
            InitializeModListInGUI();
        }
    }

    public void optionsGUI() {
        new Waldorf().Action(this);
        frame.setEnabled(false);
    }

    public void deleteSelected() {
        if (modList.getSelectedIndex() >= 0) {
            File file = new File(System.getProperty("user.home") + "/.firestar/mods/" + Main.Mods.get(modList.getSelectedIndex()).path);
            file.delete();
            System.out.println("Deleted " + Main.Mods.get(modList.getSelectedIndex()).friendlyName); //debug
            Main.Mods.remove(modList.getSelectedIndex());
            regenerateModBlacklist(false);
            regenerateModIndex(true);
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a mod to delete first.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void generatorGUI() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile().isDirectory()
            && new File(fileChooser.getSelectedFile().getAbsolutePath() + "/data").isDirectory()) {
                File file = fileChooser.getSelectedFile();
                new Clifford().Action(this, file);
                frame.setEnabled(false);
            } else {
                JOptionPane.showMessageDialog(frame, "You must select a folder containing a \"data\" directory with game assets.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void metaEditorGUI(int index) {
        if (index >= 0) {
            new Clifford().Action(this, index);
            frame.setEnabled(false);
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a mod to edit first.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void moveUp(int index) {
        if (index > 0) {
            Collections.swap(Main.Mods, index, index - 1);
            System.out.println("Items moved, redeploying list");
            InitializeModListInGUI();
            regenerateModIndex(false);
        }
    }

    private void moveDown(int index) {
        if (index < (Main.Mods.size() - 1)) {
            Collections.swap(Main.Mods, index, index + 1);
            System.out.println("Items moved, redeploying list");
            InitializeModListInGUI();
            regenerateModIndex(false);
        }
    }

    private void toggleSelected(int index) {
        if (index >= 0) {
            Main.Mods.get(index).enabled = !Main.Mods.get(index).enabled;
            regenerateModBlacklist(true);
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a mod to toggle first.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void throwUnimplemented() {
        JOptionPane.showMessageDialog(frame, "This feature is unimplemented and will be coming soon.\nSee README at https://git.worlio.com/bonkmaykr/firestar", "Unimplemented", JOptionPane.INFORMATION_MESSAGE);
    }

    public void createSelectionEventListener() { // moved incase needs to be removed and re-added
        listenersAlreadySet = true; // was written to troubleshoot a bug but this wasn't actually the cause
        modList.addListSelectionListener(e -> {
            if (modList.getSelectedIndex() >= 0 && modList.getModel().getSize() >= 1) { // avoid race OOB when reinitializing mod list
            String authorDisplay;

            try { //debug

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
            if (Main.Mods.get(modList.getSelectedIndex()).author == null || Main.Mods.get(modList.getSelectedIndex()).author.isEmpty()) {
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
            );}

            catch (IndexOutOfBoundsException ex) {
                System.out.println(ex.getMessage());
                System.out.println("mods " + Main.Mods.size());
                System.out.println("mod display " + modList.getModel().getSize());
                System.out.println("selection index " + modList.getSelectedIndex());

                int result = JOptionPane.showConfirmDialog(frame, "Firestar encountered an internal error.\n" + ex.getMessage(), "Fatal Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {System.exit(1);} //user safety
            }

            }
        });
    }

    public void regenerateModIndex(boolean reload) {
        try {
            System.out.println("Regenerating index..."); //debug

            new File(System.getProperty("user.home") + "/.firestar/mods/index").delete();
            File priorityListFileHandle = new File(System.getProperty("user.home") + "/.firestar/mods/index");
            priorityListFileHandle.createNewFile();

            BufferedWriter bw = new BufferedWriter(new FileWriter(System.getProperty("user.home") + "/.firestar/mods/index", true));
            int i = 0;
            for (Main.Mod m : Main.Mods) {
                bw.write(i + "=" + m.path);
                bw.newLine();
                i++;
            }
            bw.close();
            System.out.println("Mod index file regenerated.");

            if(reload) {
                Main.Mods.clear(); //cleanup
                priorityList = "";
                InitializeModListStructure();
                InitializeModListInGUI();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            JOptionPane.showMessageDialog(frame, "An error has occured.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void regenerateModBlacklist(boolean reload) {
        try {
            System.out.println("Regenerating blacklist..."); //debug

            new File(System.getProperty("user.home") + "/.firestar/mods/blacklist").delete();
            File blackListFileHandle = new File(System.getProperty("user.home") + "/.firestar/mods/blacklist");
            blackListFileHandle.createNewFile();

            BufferedWriter bw2 = new BufferedWriter(new FileWriter(System.getProperty("user.home") + "/.firestar/mods/blacklist", true));
            int i2 = 0;
            for (Main.Mod m : Main.Mods) {
                if (!m.enabled) {
                    bw2.write(m.path);
                    bw2.newLine();
                    i2++;
                }
            }
            bw2.close();
            System.out.println("Mod blacklist file regenerated.");

            if(reload) {
                Main.Mods.clear(); //cleanup
                blackList = "";
                InitializeModListStructure();
                InitializeModListInGUI();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            JOptionPane.showMessageDialog(frame, "An error has occured.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
