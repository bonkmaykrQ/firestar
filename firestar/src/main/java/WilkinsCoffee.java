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
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class WilkinsCoffee {
    BufferedImage windowIcon;
    public JFrame frame = new JFrame();
    private JPanel frameContainer;
    private JLabel picLabel;
    private JEditorPane instructions;
    private JButton contBtn;

    Image logo;

    public void setup() {
        try {
            windowIcon = ImageIO.read(Main.class.getResourceAsStream("/titleIcon.png"));
            frame.setIconImage(windowIcon);
        } catch (IOException e) {
            System.out.println("ERROR: Failed to find /resources/titleIcon.png. Window will not have an icon.");
        }
        try {
            logo = ImageIO.read(Main.class.getResourceAsStream("/programIcon.png")).getScaledInstance(76, 76, Image.SCALE_SMOOTH);
            picLabel.setIcon(new ImageIcon(logo));picLabel.setText("");
        } catch (IOException e) {
            System.out.println("ERROR: Missing resource in Wilkins. Page will be without images.");
            picLabel.setText("");
        }

        String bodyRule = "html, body {margin:0px;} p#first-p {margin-top:-13px;} p {margin-left:-20px;}";
        ((HTMLDocument)instructions.getDocument()).getStyleSheet().addRule(bodyRule);
        instructions.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE); // makes text smaller than joe biden's windmill if font set manually. wtf??
        contBtn.setFont(Main.fExo2.deriveFont(Font.BOLD).deriveFont(12f));

        frame.add(frameContainer); // initialize window contents -- will be handled by IntelliJ IDEA

        frame.setSize(400, 400);
        frame.setTitle("Firestar Setup");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
