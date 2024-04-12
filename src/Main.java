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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import javax.swing.*;

public class Main {
    // Build Information
    public static final String vstr = "debug";
    public static final String vcode = "Natasha";
    public static final int vint = 0;

    // User Settings
    public static String outpath; //game assets location
    public static String inpath = System.getProperty("user.home") + "/.firestar/"; //game assets location
    public static boolean repatch; //are we in compat mode?
    public static boolean wine; //are we on Linux, MINIX, BSD?
    //public static String psarc; //sdk location

    public class Mod {
        public String path;
        public int version = 1;
        //public int gameversion; //TODO detect a game version and compatibility? // no
        public int priority = 0;
        public String friendlyName;
        public String game; //TODO for multi game support
        public int loaderversion = 0; //minimum required vint or feature level from Firestar
        public String author; // if null, "Author is unknown."
    }

    // Mods
    public static List<Mod> Mods = new ArrayList<Mod>();

    public static void main(String[] args) {
        // license string
        System.out.printf("FIRESTAR MOD MANAGER for WipEout 2048\nversion " + vstr + " (codename " + vcode + ")\n" +
                "JVM host appears to be " + System.getProperty("os.name") +
                "\nCopyright (C) 2024  bonkmaykr\n\nThis program is free software: you can redistribute it and/or modify\n" +
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
        // check and load configs
        File fConf = new File(System.getProperty("user.home") + "/.firestar/firestar.conf");
        if (!fConf.isFile()) {
            new Kermit().setup(fConf); // this is a fresh install, run the OOBE.
        } else {
            // todo load modlist
            new MissPiggy().Action(); // Quick! Start singing Firework by Katy Perry! (or open the main window i guess...)
        }
    }

    public static void writeConf(){
        JSONObject container = new JSONObject();
        container.put("version", vint);
        container.put("2048path", outpath);
        container.put("HDpath", "TODO"); // proposed hd/fury support for ps3, will use very simplified Fast Mode due to less difficulty installing
        container.put("safemode", repatch);
        container.put("isUnix", wine);
        container.put("currentPlaylist", "TODO"); // proposed feature: store separate mod lists in lists/ to load/save later?

        try {
            Files.write(Paths.get(System.getProperty("user.home") + "/.firestar/firestar.conf"), container.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadConf(){

    }
}