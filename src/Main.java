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

import org.json.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.swing.*;

public class Main {
    public static String vstr = "debug";
    public static String vcode = "Natasha";
    public static int vint = 0;

    public static String path; //game assets location
    public static boolean repatch; //are we in compat mode?
    //public static String psarc; //sdk location

    public static void main(String[] args) {
        // license string
        System.out.printf("FIRESTAR MOD MANAGER for WipEout 2048\nversion " + vstr + " (codename " + vcode + ")\nCopyright (C) 2024  bonkmaykr\n\nThis program is free software: you can redistribute it and/or modify\n" +
                "it under the terms of the GNU General Public License as published by\n" +
                "the Free Software Foundation, either version 3 of the License, or\n" +
                "(at your option) any later version.\n" +
                "\n" +
                "This program is distributed in the hope that it will be useful,\n" +
                "but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
                "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
                "GNU General Public License for more details.\n" +
                "\n" +
                "You should have received a copy of the GNU General Public License\n" +
                "along with this program.  If not, see https://www.gnu.org/licenses/.\n\n\n\n");

        //begin
        // todo create main window

        // check and load configs
        File fConf = new File(System.getProperty("user.home") + "/.firestar/firestar.conf");
        if (!fConf.isFile()) {
            new Kermit().setup(fConf); // this is a fresh install, run the OOBE.
        }

        // todo load modlist and send to MissPiggy
    }
}