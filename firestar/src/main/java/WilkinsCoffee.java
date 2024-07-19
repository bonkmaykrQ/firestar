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
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class WilkinsCoffee implements ActionListener {
    Image logo;
    public Pages page;
    public JFrame frame = new JFrame();
    private JPanel frameContainer;
    private JLabel picLabel;
    private JEditorPane instructions;
    private JButton contBtn;
    private JPanel inputContainer;
    private JButton PSARC_downBtn;
    private JButton PSARC_impBtn;
    private JPanel inputContainer2;
    private JButton EXPORT_openFolderBtn;
    private JLabel pathDisplay;
    private JPanel checklistContainer;
    private JLabel checklistFury;
    private JLabel checklistHD;
    private JLabel checklistPatch2;
    private JLabel checklistPatch1;
    private JLabel checklistBase;

    public enum Pages {
        INTRO(0),
        PSARC(1),
        EXPORT_LOCATION(2),
        DONE(3);

        public final int value;

        private Pages(int i) {
            this.value = i;
        }
    }

    private String outPathTemp = System.getProperty("user.home") + "/Documents/";
    private boolean sdkInstalled = false;

    public void setup() {
        page = Pages.INTRO;
        inputContainer.setVisible(false);
        checklistContainer.setVisible(false);
        inputContainer2.setVisible(false);

        // check if this is windows or not
        if(System.getProperty("os.name").contains("Windows")) {Main.windows = true;System.out.println("Assuming we should NOT use WINE based on known system variables.");}
        else {Main.windows = false;System.out.println("Assuming we should use WINE based on known system variables.");}

        // reformat path slightly for windows
        if (Main.windows) {outPathTemp.replace("/", "\\");}

        frame.setIconImage(Main.windowIcon);
        try {
            logo = ImageIO.read(Main.class.getResourceAsStream("/programIcon.png")).getScaledInstance(96, 96, Image.SCALE_SMOOTH);
            picLabel.setIcon(new ImageIcon(logo));picLabel.setText("");
        } catch (IOException e) {
            System.out.println("ERROR: Missing resource in Wilkins. Page will be without images.");
            picLabel.setText("");
        }

        String bodyRule = "html, body {margin:0px;} p#first-p {margin-top:-13px;} p {margin-left:-20px;}";
        ((HTMLDocument)instructions.getDocument()).getStyleSheet().addRule(bodyRule);
        instructions.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE); // makes text smaller than joe biden's windmill if font set manually. wtf??
        contBtn.setFont(Main.fExo2.deriveFont(Font.BOLD).deriveFont(12f));
        PSARC_downBtn.setFont(Main.fExo2.deriveFont(Font.BOLD).deriveFont(12f));
        PSARC_impBtn.setFont(Main.fExo2.deriveFont(Font.BOLD).deriveFont(12f));
        EXPORT_openFolderBtn.setFont(Main.fExo2.deriveFont(Font.BOLD).deriveFont(12f));

        frame.add(frameContainer); // initialize window contents -- will be handled by IntelliJ IDEA

        contBtn.addActionListener(this);
        PSARC_downBtn.addActionListener(this);
        PSARC_impBtn.addActionListener(this);
        EXPORT_openFolderBtn.addActionListener(this);

        frame.setSize(400, 400);
        frame.setTitle("Firestar Setup");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        WilkinsCoffee threadParent = this;
        if (actionEvent.getSource() == PSARC_downBtn) {
            Thread waiterThread = new Thread(new Runnable() {@Override
            public void run() {
                if (new Bert(frame).reportWhenDownloaded(threadParent)) {
                    contBtn.setEnabled(true);
                    contBtn.setBackground(new Color(221, 88, 11)); //orange

                    refreshChecklist();
                }
            }});
        waiterThread.start();
        } else
        if (actionEvent.getSource() == PSARC_impBtn) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            FileFilter fileChooserFilter = new FileNameExtensionFilter("Sony Playstation Archive File", "psarc");
            fileChooser.setFileFilter(fileChooserFilter);
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    Files.copy(fileChooser.getSelectedFile().toPath(), new File(Main.inpath + fileChooser.getSelectedFile().getName()).toPath());
                    refreshChecklist();
                    contBtn.setEnabled(true);
                    contBtn.setBackground(new Color(221, 88, 11)); //orange
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    JOptionPane.showMessageDialog(frame, "An error has occured.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else
        if (actionEvent.getSource() == EXPORT_openFolderBtn) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                if (fileChooser.getSelectedFile().isDirectory()) {
                    outPathTemp = fileChooser.getSelectedFile().getAbsolutePath()+"/";
                    pathDisplay.setText("Export path: " + outPathTemp);
                }
            }
        } else
        if (actionEvent.getSource() == contBtn) {
            switch (page) {
                case INTRO:
                    if (!new File(Main.inpath + "psp2psarc.exe").exists()) { // we may have been here before // nag
                        frame.setEnabled(false);
                        int result = JOptionPane.showConfirmDialog(frame, "Firestar needs to download additional software to function. Setup is automatic and will only take a few minutes.\nIf you select NO, you will have to download additional dependencies later on.\n\nContinue?", "Firestar Setup", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                        if (result == JOptionPane.YES_OPTION) {
                            sdkInstalled = true;
                            Thread downloaderPopupThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Main.downloadDependenciesBeforeSetVisible(frame);
                                    frame.setEnabled(true);
                                }
                            });
                            downloaderPopupThread.start();
                        } else {
                            frame.setEnabled(true);
                        }
                    } else {
                        sdkInstalled = true;
                    }

                    page = Pages.PSARC;
                    try {
                        logo = ImageIO.read(Main.class.getResourceAsStream("/setupIconPSARC.png")).getScaledInstance(96, 96, Image.SCALE_SMOOTH);
                        picLabel.setIcon(new ImageIcon(logo));picLabel.setText("");
                    } catch (IOException e) {
                        System.out.println("ERROR: Missing resource in Wilkins. Page will be without images.");
                        picLabel.setText("");
                    }

                    contBtn.setEnabled(false);
                    contBtn.setBackground(new Color(102, 74, 58)); //brown
                    refreshChecklist();
                    inputContainer.setVisible(true);
                    checklistContainer.setVisible(true);
                    if (!sdkInstalled) {
                        PSARC_downBtn.setEnabled(false);
                        PSARC_downBtn.setBackground(new Color(102, 74, 58)); //brown
                    }

                    instructions.setText("<html>\n" +
                            "  <head>\n" +
                            "    \n" +
                            "  </head>\n" +
                            "  <body>\n" +
                            "    <p id=\"first-p\">\n" +
                            "      You must dump the original assets from WipEout 2048 so that Firestar can patch them.<br><br>If you would like, you can choose to have these downloaded and extracted for you, or you can provide your own decrypted dumps.</p><p>To decrypt your own dumps, please see \"Decrypting Original PSARC Files\" in the manual.\n" +
                            "    </p>\n" +
                            "    <p>\n" +
                            "      Press &quot;Continue&quot; when you are done.\n" +
                            "    </p>\n" +
                            "  </body>\n" +
                            "</html>\n");

                    break;
                case PSARC:
                    page = Pages.EXPORT_LOCATION;
                    try {
                        logo = ImageIO.read(Main.class.getResourceAsStream("/setupIconEXPORT.png")).getScaledInstance(96, 96, Image.SCALE_SMOOTH);
                        picLabel.setIcon(new ImageIcon(logo));picLabel.setText("");
                    } catch (IOException e) {
                        System.out.println("ERROR: Missing resource in Wilkins. Page will be without images.");
                        picLabel.setText("");
                    }
                    contBtn.setEnabled(true);
                    contBtn.setBackground(new Color(221, 88, 11)); //orange
                    inputContainer.setVisible(false);
                    checklistContainer.setVisible(false);
                    inputContainer2.setVisible(true);

                    instructions.setText("<html>\n" +
                            "  <head>\n" +
                            "    \n" +
                            "  </head>\n" +
                            "  <body>\n" +
                            "    <p id=\"first-p\">\n" +
                            "      Almost done!<br><br>Select the folder you would like to export your compiled mods to.<br><br>When you select \"Deploy\" in the mod list, Firestar will place a PSARC file into this folder which you can install onto your Vita.\n" +
                            "    </p>\n" +
                            "    <p>\n" +
                            "      Press &quot;Continue&quot; when you are done.\n" +
                            "    </p>\n" +
                            "  </body>\n" +
                            "</html>\n");

                    pathDisplay.setText("Export path: " + outPathTemp);

                    break;
                case EXPORT_LOCATION:
                    page = Pages.DONE;
                    try {
                        logo = ImageIO.read(Main.class.getResourceAsStream("/setupIconDONE.png")).getScaledInstance(96, 96, Image.SCALE_SMOOTH);
                        picLabel.setIcon(new ImageIcon(logo));picLabel.setText("");
                    } catch (IOException e) {
                        System.out.println("ERROR: Missing resource in Wilkins. Page will be without images.");
                        picLabel.setText("");
                    }
                    inputContainer2.setVisible(false);
                    Main.outpath = outPathTemp;
                    Main.repatch = true;
                    Main.writeConf();

                    instructions.setText("<html>\n" +
                            "  <head>\n" +
                            "    \n" +
                            "  </head>\n" +
                            "  <body>\n" +
                            "    <p id=\"first-p\">\n" +
                            "      Setup complete!\n" +
                            "    </p>\n" +
                            "  </body>\n" +
                            "</html>\n");

                    break;
                case DONE:
                    frame.dispose();
                    new MissPiggy().Action();
                    break;
                default:
                    throw new UnsupportedOperationException("ERROR: Setup page-flip event listener didn't drink any Wilkins Coffee. Get a programmer!");
            }
        }
    }

    private void refreshChecklist() {
        ImageIcon positive = new ImageIcon(Main.class.getResource("/lightPositive.png"));
        ImageIcon negative = new ImageIcon(Main.class.getResource("/lightNegative.png"));

        // enabling the continue button here leaves the previous one redundant,
        // but it's needed to ensure we don't force a redownload if the setup is interrupted

        if(new File(Main.inpath + "data.psarc").exists()) {
            checklistBase.setIcon(positive);
            contBtn.setEnabled(true);
            contBtn.setBackground(new Color(221, 88, 11)); //orange
        } else {checklistBase.setIcon(negative);}
        if(new File(Main.inpath + "data1.psarc").exists()) {
            checklistPatch1.setIcon(positive);
            contBtn.setEnabled(true);
            contBtn.setBackground(new Color(221, 88, 11)); //orange
        } else {checklistPatch1.setIcon(negative);}
        if(new File(Main.inpath + "data2.psarc").exists()) {
            checklistPatch2.setIcon(positive);
            contBtn.setEnabled(true);
            contBtn.setBackground(new Color(221, 88, 11)); //orange
        } else {checklistPatch2.setIcon(negative);}
        if(new File(Main.inpath + "dlc1.psarc").exists()) {
            checklistHD.setIcon(positive);
            contBtn.setEnabled(true);
            contBtn.setBackground(new Color(221, 88, 11)); //orange
        } else {checklistHD.setIcon(negative);}
        if(new File(Main.inpath + "dlc2.psarc").exists()) {
            checklistFury.setIcon(positive);
            contBtn.setEnabled(true);
            contBtn.setBackground(new Color(221, 88, 11)); //orange
        } else {checklistFury.setIcon(negative);}
    }
}
