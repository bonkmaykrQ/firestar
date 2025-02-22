/*
 *     Firestar Mod Manager
 *     Copyright (C) 2025  Canithesis Interactive
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
import java.awt.*;
import java.io.IOException;

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class Beaker {
    Image logo;
    private JLabel splashImg;
    JFrame frame = new JFrame();
    JPanel frameContainer;

    public Beaker() throws InterruptedException {
        try {
            logo = ImageIO.read(Main.class.getResourceAsStream("/splash"+(int)(Math.random()*11)+".png")).getScaledInstance(640, 400, Image.SCALE_SMOOTH);
        } catch (IOException e) {
            System.out.println("ERROR: Uhhhhhhhhhhh........... what?");
            return;
        }

        frame.add(frameContainer); // initialize window contents -- will be handled by IntelliJ IDEA
        splashImg.setIcon(new ImageIcon(logo));splashImg.setText("");

        // display window
        frame.setSize(640, 480);
        frame.setTitle("Firestar Mod Manager");
        frame.setUndecorated(true);
        frame.setResizable(false);
        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        Thread.sleep(2000);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.dispose();
    }
}
