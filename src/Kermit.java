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
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.*;
import java.security.*;
import java.util.concurrent.TimeUnit;

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

// handles setup window
public class Kermit implements ActionListener {
    public enum Pages {
        AGREEMENT(0),
        EXPORT_MODE(1),
        SDK_INSTALL(2),
        SDK_FAILURE(3),
        WHAT_OS(4),
        EXPORT_LOCATION(5),
        IMPORT_LOCATION(6),
        DONE(7);

        public final int value;

        private Pages(int i) {
            this.value = i;
        }
    }

    JDialog frame = new JDialog();
    JButton button = new JButton("Next");
    JButton button2 = new JButton("Quit");
    JButton button3 = new JButton("Cancel");
    JButton button4 = new JButton("Back");
    JTextPane dialogText = new JTextPane();
    JRadioButton buttonCompat = new JRadioButton("Compatibility (for consoles)");
    JRadioButton buttonFast = new JRadioButton("Fast Mode (for Vita3K emulator)");
    ButtonGroup radg1 = new ButtonGroup();
    JRadioButton buttonNoWine = new JRadioButton("No, I use Microsoft Windows");
    JRadioButton buttonHaveWine = new JRadioButton("Yes, I have a POSIX system with WINE.");
    ButtonGroup radg2 = new ButtonGroup();
    JTextField pathInput = new JTextField();
    JButton openconfigfolderbutton = new JButton("Open Firestar Folder");
    Pages page = Pages.AGREEMENT;
    javax.swing.text.StyledDocument document = dialogText.getStyledDocument();
    javax.swing.text.SimpleAttributeSet align= new javax.swing.text.SimpleAttributeSet();

    public void setup(File fConf) { //File variable is redundant. unused
        // todo Disable MissPiggy

        frame.getContentPane().setBackground(Color.WHITE);
        radg1.add(buttonCompat);
        radg1.add(buttonFast);
        radg2.add(buttonNoWine);
        radg2.add(buttonHaveWine);

        button.addActionListener(this);
        button2.addActionListener(this);
        button3.addActionListener(this);
        button4.addActionListener(this);
        buttonCompat.addActionListener(this);
        buttonFast.addActionListener(this);
        buttonNoWine.addActionListener(this);
        buttonHaveWine.addActionListener(this);
        openconfigfolderbutton.addActionListener(this);

        changePage(Pages.AGREEMENT);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == button2){
            System.exit(0);
        } else if (actionEvent.getSource() == button){
            //frame.removeAll(); freezes??
            switch (page) {
                case AGREEMENT:
                    button.setVisible(false);frame.remove(button);
                    button2.setVisible(false);frame.remove(button2);
                    dialogText.setVisible(false);frame.remove(dialogText);
                    changePage(Pages.EXPORT_MODE);
                    break;
                case EXPORT_MODE:
                    button.setVisible(false);frame.remove(button);
                    button3.setVisible(false);frame.remove(button3);
                    dialogText.setVisible(false);frame.remove(dialogText);
                    buttonCompat.setVisible(false);frame.remove(buttonCompat);
                    buttonFast.setVisible(false);frame.remove(buttonFast);
                    if (Main.repatch) {
                        changePage(Pages.SDK_INSTALL);
                    } else {changePage(Pages.EXPORT_LOCATION);}
                    break;
                case SDK_INSTALL:
                    break;
                case SDK_FAILURE:
                    break;
                case WHAT_OS:
                    button.setVisible(false);frame.remove(button);
                    button3.setVisible(false);frame.remove(button3);
                    dialogText.setVisible(false);frame.remove(dialogText);
                    break;
                case EXPORT_LOCATION:
                    button.setVisible(false);frame.remove(button);
                    button3.setVisible(false);frame.remove(button3);
                    dialogText.setVisible(false);frame.remove(dialogText);
                    buttonNoWine.setVisible(false);frame.remove(buttonNoWine);
                    buttonHaveWine.setVisible(false);frame.remove(buttonHaveWine);
                    changePage(Pages.IMPORT_LOCATION);
                    break;
                case IMPORT_LOCATION:
                    Main.outpath = pathInput.getText();
                    pathInput.setText("");
                    dialogText.setVisible(false);frame.remove(dialogText);
                    pathInput.setVisible(false);frame.remove(pathInput);
                    changePage(Pages.DONE);
                    break;
                case DONE:
                    new MissPiggy().Action();

                    page = Pages.AGREEMENT; //set it here since we're disposing of the entire thing
                    frame.dispose();
                    break;
                default:
                    throw new UnsupportedOperationException("ERROR: Undefined behavior in Kermit's event listener. Get a programmer!");
                    //JOptionPane OhShit = new JOptionPane.showMessageDialog(null, "Fuck");
            }
        } else if (actionEvent.getSource() == button4){
            //frame.removeAll(); freezes??
            switch (page) { // todo remove elements when going to previous page
                case EXPORT_MODE:
                    changePage(Pages.AGREEMENT);
                    break;
                case SDK_INSTALL:
                    changePage(Pages.EXPORT_MODE);
                    break;
                case SDK_FAILURE:
                    break;
                case WHAT_OS:
                    changePage(Pages.SDK_INSTALL);
                    break;
                case EXPORT_LOCATION:
                    if (Main.repatch) {changePage(Pages.WHAT_OS);} else {changePage(Pages.EXPORT_MODE);}
                    break;
                case IMPORT_LOCATION:
                    changePage(Pages.EXPORT_LOCATION);
                    break;
                default:
                    throw new UnsupportedOperationException("ERROR: Undefined behavior in Kermit's event listener. Get a programmer!");
                    //JOptionPane OhShit = new JOptionPane.showMessageDialog(null, "Fuck");
            }
        } else if (actionEvent.getSource() == buttonCompat) {
            Main.repatch = true;
            button.setEnabled(true);
        } else if (actionEvent.getSource() == buttonFast) {
            Main.repatch = false;
            button.setEnabled(true);
        } else if (actionEvent.getSource() == buttonHaveWine) {
            Main.wine = true;
            button.setEnabled(true);
        } else if (actionEvent.getSource() == buttonNoWine) {
            Main.wine = false;
            button.setEnabled(true);
        }else if (actionEvent.getSource() == openconfigfolderbutton) {
            try {
                Desktop.getDesktop().open(new File(Main.inpath));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void changePage(Pages GoTo){
        switch (GoTo) {
            case AGREEMENT:
                page = Pages.AGREEMENT;

                button.setVisible(true);
                button.setBounds(292, 343, 300, 30);
                frame.add(button);

                button2.setVisible(true);
                button2.setBounds(0, 343, 292, 30);
                frame.add(button2);

                dialogText.setVisible(true);
                dialogText.setHighlighter(null);
                dialogText.getCaret().setVisible(false);
                dialogText.setFocusable(false);
                dialogText.setBounds(0, 0, 592, 343);
                //dialogText.setAlignmentX(JComponent.CENTER_ALIGNMENT);
                //dialogText.setAlignmentY(JComponent.CENTER_ALIGNMENT);
                StyleConstants.setAlignment(align, StyleConstants.ALIGN_CENTER);
                document.setParagraphAttributes(0, document.getLength(), align, false);
                //dialogText.setText("Aww Fiddlesticks, what now?");
                dialogText.setText("WELCOME TO FIRESTAR\n\n" +
                        "This initial setup guide will help you prepare your Playstation Vita, Playstation TV, or Vita3K emulator to play WipEout mods. You will be asked a series of questions to help Firestar decide the most optimal installation method for you.\n\n" +
                        "If you encounter any issues while using Firestar you may contact the author at bonkmaykr@screwgravity.net\n\n" +
                        "DISCLAIMER: This program is free software licensed under the GNU General Public License version 3.0. You may share this program and it's source code so long as you extend the same rights to others. The developers of this software are never responsible for any damage to your game console and Firestar is provided to you without any warranty or legal guarantee. By using Firestar, you agree to these terms. For more information, visit https://www.gnu.org/licenses/gpl-3.0.en.html");
                frame.add(dialogText);

                frame.setSize(600, 400);
                frame.setTitle("Initial Setup");
                frame.setAlwaysOnTop(true);
                frame.setDefaultCloseOperation(0);
                frame.setResizable(false);
                frame.setLayout(null);
                frame.setVisible(true);
                break;
            case EXPORT_MODE:
                page = Pages.EXPORT_MODE;

                button.setVisible(true);
                button.setEnabled(false);
                button.setBounds(292, 343, 300, 30);
                frame.add(button);

                button3.setVisible(true);
                button3.setBounds(0, 343, 292, 30);
                frame.add(button3);

                dialogText.setVisible(true);
                dialogText.setHighlighter(null);
                dialogText.getCaret().setVisible(false);
                dialogText.setFocusable(false);
                dialogText.setBounds(0, 40, 592, 150);
                StyleConstants.setAlignment(align, StyleConstants.ALIGN_CENTER);
                document.setParagraphAttributes(0, document.getLength(), align, false);
                dialogText.setText("Please choose how Firestar will deploy your mods.\n\n" +
                        "Compatibility mode requires software from the PSVita SDK, but works on real hardware.\n" +
                        "Fast mode is easiest, but won't work on FAT32/exFAT drives like what the Vita uses.");
                frame.add(dialogText);

                buttonCompat.setBounds(40, 200, 300, 25);
                buttonFast.setBounds(40, 230, 300, 25);
                buttonCompat.setBackground(Color.WHITE);
                buttonFast.setBackground(Color.WHITE);
                buttonCompat.setVisible(true);
                buttonFast.setVisible(true);
                frame.add(buttonCompat);
                frame.add(buttonFast);

                frame.setSize(600, 400);
                frame.setTitle("Initial Setup");
                frame.setAlwaysOnTop(true);
                frame.setDefaultCloseOperation(0);
                frame.setResizable(false);
                frame.setLayout(null);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                break;
            case SDK_INSTALL:
                page = Pages.SDK_INSTALL;

                dialogText.setVisible(true);
                dialogText.setHighlighter(null);
                dialogText.getCaret().setVisible(false);
                dialogText.setFocusable(false);
                dialogText.setBounds(0, 40, 592, 300);
                StyleConstants.setAlignment(align, StyleConstants.ALIGN_CENTER);
                document.setParagraphAttributes(0, document.getLength(), align, false);
                dialogText.setText("Firestar is downloading important dependencies. Please wait."); //some kind of race condition prevents this from displaying?
                frame.add(dialogText);

                frame.setSize(600, 400);
                frame.setTitle("Initial Setup");
                frame.setAlwaysOnTop(true);
                frame.setDefaultCloseOperation(0);
                frame.setResizable(false);
                frame.setLayout(null);
                frame.setVisible(true);

                //md5 checksum 4ef707b2dba6944a726d46950aaddfd2
                try {
                    File downloadLocationDir = new File(System.getProperty("user.home") + "/.firestar/");
                    File downloadLocation = new File(System.getProperty("user.home") + "/.firestar/psp2psarc.exe");
                    downloadLocationDir.mkdirs();
                    if (!downloadLocation.isFile()) {
                        downloadLocation.createNewFile();
                    }
                    BufferedInputStream in = new BufferedInputStream(new URL("http://bonkmaykr.worlio.com/http/firestar/psp2psarc.exe").openStream());
                    //FileOutputStream downloadOutput = new FileOutputStream(new File(System.getProperty("user.home") + "/.firestar/psp2psarc.exe"));
                    Files.copy(in, Paths.get(System.getProperty("user.home") + "/.firestar/psp2psarc.exe"), StandardCopyOption.REPLACE_EXISTING);

                    int tests = 0;
                    String checksum ="";
                    while (tests < 60 /*while(true)*/) {
                        byte[] hash = MessageDigest.getInstance("MD5").digest(Files.readAllBytes(Paths.get(System.getProperty("user.home") + "/.firestar/psp2psarc.exe")));
                        checksum = new BigInteger(1, hash).toString(16);
                        System.out.println("Downloaded psp2psarc.exe successfully.");
                        if(checksum.equals("4ef707b2dba6944a726d46950aaddfd2")) {changePage(Pages.WHAT_OS);break;}
                        Thread.sleep(20);
                        tests++;
                    }

                    if(checksum.equals("4ef707b2dba6944a726d46950aaddfd2")) {changePage(Pages.WHAT_OS);} else {
                        System.out.println("ERROR: Failed to download PSARC tool. Check connection and ensure the file is not corrupt or infected.");
                        dialogText.setText("Firestar tried to download important files needed for operation, but they were either corrupted or did not finish.\n" + "\n\nYou will need to manually install psp2psarc.exe into your Firestar config folder after setup is complete! Or, you can retry the download later.");
                        //frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

                        button.setVisible(true);
                        button.setBounds(292, 343, 300, 30);
                        frame.add(button);
                    }
                } catch (Exception e) {
                    System.out.println("Failed to download PSARC tool due to an internal error:" + e.getMessage());
                    dialogText.setText("An error has occured.\n" + e.getMessage() + "\n\nYou will need to manually install psp2psarc.exe into your Firestar config folder after setup is complete!!!");
                    //frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

                    button.setVisible(true);
                    button.setBounds(292, 343, 300, 30);
                    frame.add(button);
                }

                break;
            case SDK_FAILURE:
                page = Pages.SDK_FAILURE; //unused
                break;
            case WHAT_OS:
                page = Pages.WHAT_OS;

                //do window clear here since the page is never called by the event handler
                button.setVisible(false);frame.remove(button);
                button3.setVisible(false);frame.remove(button3);
                dialogText.setVisible(false);frame.remove(dialogText);

                //check if this is windows or not
                if(System.getProperty("os.name").contains("Windows")) {Main.wine = false;System.out.println("Assuming we should NOT use WINE based on known system variables.");changePage(Pages.EXPORT_LOCATION);}
                else {Main.wine = true;System.out.println("Assuming we should use WINE based on known system variables.");changePage(Pages.EXPORT_LOCATION);}

            case EXPORT_LOCATION:
                page = Pages.EXPORT_LOCATION;

                button.setVisible(true);
                button.setBounds(292, 343, 300, 30);
                frame.add(button);

                button3.setVisible(true);
                button3.setBounds(0, 343, 292, 30);
                frame.add(button3);

                dialogText.setVisible(true);
                dialogText.setHighlighter(null);
                dialogText.getCaret().setVisible(false);
                dialogText.setFocusable(false);
                dialogText.setBounds(30, 40, 542, 150);
                StyleConstants.setAlignment(align, StyleConstants.ALIGN_CENTER);
                document.setParagraphAttributes(0, document.getLength(), align, false);
                dialogText.setText("Now enter the location of your game's asset folder.\n" +
                        "This can be your install directory on your emulator, your repatch folder on your Vita memory card, or a temporary directory you'd like to copy over yourself.\n\n" +
                        "PLEASE DO NOT POINT THIS DIRECTLY TO WHERE THE GAME IS INSTALLED ON A REAL VITA\n" +
                        "(ux0:/app, ux0:/patch or ux0:/addcont)\n" +
                        "AS THIS WILL CORRUPT YOUR GAME AND YOU WILL NEED TO REINSTALL IT.\nDoing this on an emulator is fine.");
                frame.add(dialogText);

                pathInput.setVisible(true);
                pathInput.setBounds(30,200,542,30);
                frame.add(pathInput);

                frame.setSize(600, 400);
                frame.setTitle("Initial Setup");
                frame.setAlwaysOnTop(true);
                frame.setDefaultCloseOperation(0);
                frame.setResizable(false);
                frame.setLayout(null);
                frame.setVisible(true);
                break;
            case IMPORT_LOCATION:
                // I think for Fast Mode this step may be unnecessary? look into alternatives perhaps

                page = Pages.IMPORT_LOCATION;
                pathInput.setVisible(false); //GET OUT OF MY HEAD

                button.setVisible(true);
                button.setBounds(292, 343, 300, 30);
                frame.add(button);

                button3.setVisible(true);
                button3.setBounds(0, 343, 292, 30);
                frame.add(button3);

                dialogText.setVisible(true);
                dialogText.setHighlighter(null);
                dialogText.getCaret().setVisible(false);
                dialogText.setFocusable(false);
                dialogText.setBounds(30, 40, 542, 200);
                StyleConstants.setAlignment(align, StyleConstants.ALIGN_CENTER);
                document.setParagraphAttributes(0, document.getLength(), align, false);
                dialogText.setText("You're almost done!\n\n" +
                        "Please move all of your original PSARC files for WipEout (base game, patches, and HD Fury DLC) to the config folder and press Next when you are done.\n" +
                        "Firestar will use these to generate new PSARCs in place of the old ones.\n\n" +
                        "If you play on a real console, you will need to use VitaShell (remember to \"Open Decrypted\"). Check /app, /patch, and /addcont. Getting all of them is important since WipEout always loads them in a specific order and which ones you have determines where Firestar can compress the modified files to.");
                frame.add(dialogText);

                //pathInput.setVisible(true);
                //pathInput.setBounds(30,200,542,30);
                //frame.add(pathInput);
                openconfigfolderbutton.setVisible(true);
                openconfigfolderbutton.setBounds(30,250,542,30);
                frame.add(openconfigfolderbutton);

                frame.setSize(600, 400);
                frame.setTitle("Initial Setup");
                frame.setAlwaysOnTop(true);
                frame.setDefaultCloseOperation(0);
                frame.setResizable(false);
                frame.setLayout(null);
                frame.setVisible(true);
                break;
            case DONE:
                page = Pages.DONE;

                Main.writeConf(); // save changes

                //SERIOUS!!!!!!!!!!!!!! cleanup
                button.setVisible(false);frame.remove(button);
                button2.setVisible(false);frame.remove(button2);
                button3.setVisible(false);frame.remove(button3);
                button4.setVisible(false);frame.remove(button4);
                openconfigfolderbutton.setVisible(false);frame.remove(openconfigfolderbutton);
                pathInput.setVisible(false);frame.remove(pathInput);
                buttonFast.setVisible(false);frame.remove(buttonFast);
                buttonCompat.setVisible(false);frame.remove(buttonCompat);
                buttonHaveWine.setVisible(false);frame.remove(buttonHaveWine);
                buttonNoWine.setVisible(false);frame.remove(buttonNoWine);

                //congrations, your did it (party time)
                dialogText.setVisible(true);
                dialogText.setHighlighter(null);
                dialogText.getCaret().setVisible(false);
                dialogText.setFocusable(false);
                dialogText.setBounds(30, 40, 542, 200);
                StyleConstants.setAlignment(align, StyleConstants.ALIGN_CENTER);
                document.setParagraphAttributes(0, document.getLength(), align, false);
                dialogText.setText("Firestar is ready!\n\n" +
                        "For technical support, email\nbonkmaykr@screwgravity.net.");
                frame.add(dialogText);

                button.setVisible(true);
                button.setBounds(292, 343, 300, 30);
                button.setText("OK");
                frame.add(button);

                frame.setSize(600, 400);
                frame.setTitle("Initial Setup");
                frame.setAlwaysOnTop(true);
                frame.setDefaultCloseOperation(0);
                frame.setResizable(false);
                frame.setLayout(null);
                frame.setVisible(true);
                break;
            default:
                throw new UnsupportedOperationException("ERROR: Undefined behavior in Kermit.changePage(). Get a programmer!");
                //JOptionPane OhShit = new JOptionPane.showMessageDialog(null, "Fuck");
        }
    }
}
