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

import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.swing.JOptionPane;

public class Main {
    // Build Information
    public static final String vstr = "Release 1.1";
    public static final String vcode = "Dekka";
    public static final int vint = 0;

    // User Settings
    // TODO: replace with user preference when config i/o is done
    // also please double check that outpath is actually valid
    public static String outpath = System.getProperty("user.home") + "/.firestar/out/"; //game assets location
    public static String inpath = System.getProperty("user.home") + "/.firestar/"; //game assets location
    public static boolean repatch; //are we in compat mode?
    public static boolean windows; //True = windows
    //public static String psarc; //sdk location

    public class Mod {
        public String path; // file name
        public int version = 1;
        //public int gameversion; //TODO detect a game version and compatibility? // no
        public int priority = 0; //unused
        public String friendlyName;
        public String description = "";
        public String game; //TODO for multi game support
        public int loaderversion = 0; //minimum required vint or feature level from Firestar
        public String author; // if null, "Author is unknown."
        public boolean enabled = true;
    }

    // Mods
    public static List<Mod> Mods = new ArrayList<Mod>();

    // UI Global Assets
    public static Font fExo2;

    public static void main(String[] args) {
        // license string
        System.out.printf("FIRESTAR MOD MANAGER for WipEout 2048\nversion " + vstr + " (codename " + vcode + ") major " + vint + "\n" +
                "JVM host appears to be " + System.getProperty("os.name") +
                "\nRunning from " + System.getProperty("user.dir") +
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
        // load global assets
        try {
            fExo2 = Font.createFont(Font.TRUETYPE_FONT, new File(System.getProperty("user.dir") + "/resources/exo2.ttf"));
        } catch (Exception e) {
            System.out.println("Font \"Exo 2\" is missing!");
            fExo2 = new Font("Arial", Font.PLAIN, 12);
        }

        // check and load configs
        File fConf = new File(System.getProperty("user.home") + "/.firestar/firestar.conf");
        if (!fConf.isFile()) {
            System.out.println("No configuration was found. Starting the initial setup");
            new Kermit().setup(fConf); // this is a fresh install, run the OOBE.
        } else {
            new MissPiggy().Action(); // Quick! Start singing Firework by Katy Perry! (or open the main window i guess...)
        }
    }

    public static void writeConf(){
        JSONObject container = new JSONObject();
        container.put("version", vint);
        container.put("2048path", outpath);
        container.put("HDpath", "TODO"); // proposed hd/fury support for ps3, will use very simplified Fast Mode due to less difficulty installing
        container.put("safemode", repatch);
        container.put("isWin32", windows);
        container.put("currentPlaylist", "TODO"); // proposed feature: store separate mod lists in lists/ to load/save later?

        try {
            Files.write(Paths.get(System.getProperty("user.home") + "/.firestar/firestar.conf"), container.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadConf(){
        try {
            JSONObject container = new JSONObject(new String(Files.readAllBytes(Paths.get(System.getProperty("user.home") + "/.firestar/firestar.conf"))));
            System.out.println(container.toString()); // debug
            int confvint = (int) container.get("version"); // used for converting configs between program versions later down the line
            outpath = container.get("2048path").toString();
            repatch = (boolean) container.get("safemode");
            windows = (boolean) container.get("isWin32");
        } catch (IOException e) {
            System.out.println("ERROR: Failed to load firestar.conf");
            System.out.println(e.getMessage());
        }
        return;
    }

    public static void loadConf(MissPiggy w){
        try {
            JSONObject container = new JSONObject(new String(Files.readAllBytes(Paths.get(System.getProperty("user.home") + "/.firestar/firestar.conf"))));
            System.out.println(container.toString()); // debug
            int confvint = (int) container.get("version"); // used for converting configs between program versions later down the line
            outpath = container.get("2048path").toString();
            repatch = (boolean) container.get("safemode");
            windows = (boolean) container.get("isWin32");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(w.frame, "Firestar couldn't load your config file. Tread lightly.\n\n" + e.getMessage(), "Critical Error", JOptionPane.ERROR_MESSAGE);
            System.out.println("ERROR: Failed to load firestar.conf");
            System.out.println(e.getMessage());
        }
    }

    public static void deleteDir(File file) { // https://stackoverflow.com/a/29175213/9259829
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (! Files.isSymbolicLink(f.toPath())) {
                    deleteDir(f);
                }
            }
        }
        file.delete();
    }
}