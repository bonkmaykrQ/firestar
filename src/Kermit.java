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
import java.io.File;

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
    Pages page = Pages.AGREEMENT;
    javax.swing.text.StyledDocument document = dialogText.getStyledDocument();
    javax.swing.text.SimpleAttributeSet align= new javax.swing.text.SimpleAttributeSet();

    public void setup(File fConf) { //File variable is redundant. unused
        // todo Disable MissPiggy

        frame.getContentPane().setBackground(Color.WHITE);
        radg1.add(buttonCompat);
        radg1.add(buttonFast);

        button.addActionListener(this);
        button2.addActionListener(this);
        button3.addActionListener(this);
        button4.addActionListener(this);
        buttonCompat.addActionListener(this);
        buttonFast.addActionListener(this);

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
                    button2.setVisible(false);frame.remove(button3);
                    dialogText.setVisible(false);frame.remove(dialogText);
                    buttonCompat.setVisible(false);frame.remove(buttonCompat);
                    buttonFast.setVisible(false);frame.remove(buttonFast);
                    if (Main.repatch) {changePage(Pages.SDK_INSTALL);} else {changePage(Pages.EXPORT_LOCATION);}
                    break;
                case SDK_INSTALL:
                    break;
                case SDK_FAILURE:
                    break;
                case WHAT_OS:
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
        }
    }

    public void changePage(Pages GoTo) {
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

                button.setVisible(true);
                button.setBounds(292, 343, 300, 30);
                button.addActionListener(this);
                frame.add(button);

                button3.setVisible(true);
                button3.setBounds(0, 343, 292, 30);
                button3.addActionListener(this);
                frame.add(button3);

                dialogText.setVisible(true);
                dialogText.setHighlighter(null);
                dialogText.getCaret().setVisible(false);
                dialogText.setFocusable(false);
                dialogText.setBounds(0, 40, 592, 300);
                StyleConstants.setAlignment(align, StyleConstants.ALIGN_CENTER);
                document.setParagraphAttributes(0, document.getLength(), align, false);
                dialogText.setText("it works!");
                frame.add(dialogText);

                frame.setSize(600, 400);
                frame.setTitle("Initial Setup");
                frame.setAlwaysOnTop(true);
                frame.setDefaultCloseOperation(0);
                frame.setResizable(false);
                frame.setLayout(null);
                frame.setVisible(true);
                break;
            case SDK_FAILURE:
                page = Pages.SDK_FAILURE;
                break;
            case WHAT_OS:
                page = Pages.WHAT_OS;
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
