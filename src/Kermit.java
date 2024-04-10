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

    JDialog frame = new JDialog();;
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
                    changePage(Pages.IMPORT_LOCATION);
                    break;
                case IMPORT_LOCATION:
                    changePage(Pages.DONE);
                    break;
                case DONE:
                    // todo Enable MissPiggy

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
                        System.out.println("Failed to download PSARC tool because the connection stalled.");
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

                //see if we can safely assume the user's choice for them before we bother asking
                if(System.getProperty("os.name").equals("Linux")) {Main.wine = true;changePage(Pages.EXPORT_LOCATION);} else
                if(System.getProperty("os.name").contains("Windows")) {Main.wine = false;changePage(Pages.EXPORT_LOCATION);} else {

                // real stuff now
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
                dialogText.setText("Firestar was unable to detect your native operating system. Do you use WINE?");
                frame.add(dialogText);

                buttonNoWine.setBounds(40, 200, 300, 25);
                buttonHaveWine.setBounds(40, 230, 300, 25);
                buttonNoWine.setBackground(Color.WHITE);
                buttonHaveWine.setBackground(Color.WHITE);
                buttonCompat.setVisible(true);
                buttonHaveWine.setVisible(true);
                frame.add(buttonNoWine);
                frame.add(buttonHaveWine);

                frame.setSize(600, 400);
                frame.setTitle("Initial Setup");
                frame.setAlwaysOnTop(true);
                frame.setDefaultCloseOperation(0);
                frame.setResizable(false);
                frame.setLayout(null);
                frame.setVisible(true);
                }
                break;
            case EXPORT_LOCATION:
                page = Pages.EXPORT_LOCATION;
                break;
            case IMPORT_LOCATION:
                page = Pages.IMPORT_LOCATION;
                break;
            case DONE:
                page = Pages.DONE;
                break;
            default:
                throw new UnsupportedOperationException("ERROR: Undefined behavior in Kermit.changePage(). Get a programmer!");
                //JOptionPane OhShit = new JOptionPane.showMessageDialog(null, "Fuck");
        }
    }
}
