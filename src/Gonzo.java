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

public class Gonzo {
    JFrame frame = new JFrame();
    private JPanel frameContainer;
    private JTextArea consoleDisplay;

    public void DeployMods() {

        frame.add(frameContainer); // initialize window contents -- will be handled by IntelliJ IDEA

        frame.setSize(200, 100);
        frame.setMinimumSize(new Dimension(200,100));
        frame.setTitle("Mod Installation");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setLayout(new GridLayout());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
