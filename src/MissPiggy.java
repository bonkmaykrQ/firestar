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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.regex.Pattern;

import net.lingala.zip4j.*;
import net.lingala.zip4j.exception.ZipException;
import org.json.JSONObject;

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

    private int selectedItem;

    public String priorityList;

    // Initialize the main window
    public void Action(/*Main entryPoint*/) {
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
            */
            if (s.split("=")[0].matches("[0-9]+=*")) {
                //append mod to list from substring
                Main.Mod m = new Main().new Mod();
                m.path = s.substring(s.indexOf("=") + 1);
                System.out.println("found file " + m.path);

                //get json metadata from zip comment
                JSONObject metadata;
                try {
                    metadata = new JSONObject(new ZipFile(System.getProperty("user.home") + "/.firestar/mods/" + m.path).getComment());
                } catch (ZipException e) {
                    throw new RuntimeException(e); //todo: fault tolerance
                }
                if (metadata.has("friendlyName")) {m.friendlyName = metadata.get("friendlyName").toString();} else {m.friendlyName = m.path;}
                if (metadata.has("description")) {m.description = metadata.get("description").toString();}
                if (metadata.has("version")) {m.version = Integer.parseInt(metadata.get("version").toString());}
                if (metadata.has("author")) {m.author = metadata.get("author").toString();}
                if (metadata.has("loaderversion")) {m.loaderversion = Integer.parseInt(metadata.get("loaderversion").toString());}
                if (metadata.has("game")) {m.game = metadata.get("game").toString();}

                //send to list
                Main.Mods.add(m);
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
        if (actionEvent.getSource() == fileMenu.getItem(1)) {importModGUI();} else
        if (actionEvent.getSource() == fileMenu.getItem(2)) {removeAllGUI();} else
        if (actionEvent.getSource() == fileMenu.getItem(4)) {optionsGUI();} else
        if (actionEvent.getSource() == helpMenu.getItem(0)) {new Rowlf().displayAboutScreen();}
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
