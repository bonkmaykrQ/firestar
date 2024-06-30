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

import net.lingala.zip4j.ZipFile;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Gonzo {
    JFrame frame = new JFrame();
    private JPanel frameContainer;
    private JTextArea consoleDisplay;
    private JScrollPane scrollPane;
    public boolean data0;
    public boolean data1;
    public boolean data2;
    public boolean dlc1;
    public boolean dlc2;
    private MissPiggy invoker;
    public String oArcTarget = "dlc2.psarc"; // which psarc to rebuild the assets in

    public void DeployMods(MissPiggy inv) {
        invoker = inv;
        System.out.println("\n\nStarting mod deployment\n\n");

        frame.add(frameContainer); // initialize window contents -- will be handled by IntelliJ IDEA

        try {
            BufferedImage windowIcon = ImageIO.read(new File(System.getProperty("user.dir") + "/resources/titleIcon.png"));
            frame.setIconImage(windowIcon);
        } catch (IOException e) {
            System.out.println("ERROR: Failed to find /resources/titleIcon.png. Window will not have an icon.");
        }
        frame.setSize(800, 400);
        frame.setMinimumSize(new Dimension(600,400));
        frame.setTitle("Mod Installation");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setLayout(new GridLayout());
        frame.setLocationRelativeTo(null);
        frame.setAlwaysOnTop(true);
        frame.setVisible(true);

        File psarcHandle = new File(Main.inpath + "data.psarc");
        data0 = psarcHandle.isFile();
        psarcHandle = new File(Main.inpath + "data1.psarc");
        data1 = psarcHandle.isFile();
        psarcHandle = new File(Main.inpath + "data2.psarc");
        data2 = psarcHandle.isFile();
        psarcHandle = new File(Main.inpath + "dlc1.psarc");
        dlc1 = psarcHandle.isFile();
        psarcHandle = new File(Main.inpath + "dlc2.psarc");
        dlc2 = psarcHandle.isFile();

        System.out.println("Source files discovered: data " + data0 + ", data1 " + data1 + ", data2 " + data2 + ", dlc1 " + dlc1 + ", dlc2 " + dlc2);

        final Thread managerThread = new Thread() {
            @Override
            public void run() {
                if (/*Main.repatch*/true) { //todo implement fast mode correctly or remove
                    CompatibilityRoutine();
                } else {
                    FastRoutine();
                }
            }
        };
        managerThread.start();
    }

    private void CompatibilityRoutine() {
        // create temporary working area for asset dump
        new File(System.getProperty("user.home") + "/.firestar/temp/").mkdirs();

        // decide which files to dump
        List<String> dumpThese = new ArrayList<String>();
        if (data0) {dumpThese.add("data.psarc");oArcTarget = "data.psarc";}
        if (data1) {dumpThese.add("data1.psarc");oArcTarget = "data1.psarc";}
        if (data2) {dumpThese.add("data2.psarc");oArcTarget = "data2.psarc";}
        if (dlc1) {dumpThese.add("dlc1.psarc");oArcTarget = "dlc1.psarc";}
        if (dlc2) {dumpThese.add("dlc2.psarc");oArcTarget = "dlc2.psarc";}

        // dump all assets to working area
        for (String s : dumpThese) {
            try {
                System.out.println("Firestar is extracting " + s);
                consoleDisplay.append("Firestar is extracting " + s + "\n");
                //Process p = Runtime.getRuntime().exec(new String[]{"bash","-c","aplay /home/bonkyboo/kittens_loop.wav"}); // DEBUG
                Process p;
                if (!Main.windows) {p = Runtime.getRuntime().exec(new String[]{"bash","-c","cd " + System.getProperty("user.home") + "/.firestar/temp/" + ";wine ../psp2psarc.exe extract -y ../" + s});}
                else {p = Runtime.getRuntime().exec(new String[]{new String(System.getProperty("user.home") + "\\.firestar\\psp2psarc.exe"), "extract", "-y", "..\\" + s}, null, new File(new String(System.getProperty("user.home") + "/.firestar/temp/").replace("/", "\\")));}
                final Thread ioThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            final BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(p.getInputStream()));
                            String line = null;
                            while ((line = reader.readLine()) != null) {
                                System.out.println(line);
                                consoleDisplay.append(line + "\n");
                                try {scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());}
                                catch (Exception e) {System.out.println("WARNING: Swing failed to paint window due to race condition. You can safely ignore this.\n" + e.getMessage());}
                            }
                            reader.close();
                        } catch (final Exception e) {
                            e.printStackTrace(); // will probably definitely absolutely for sure hang firestar unless we do something. Too bad!
                        }
                    }
                };
                ioThread.start();
                p.waitFor();
            } catch (IOException | InterruptedException e) {
                System.out.println(e.getMessage());
                consoleDisplay.append("CRITICAL FAILURE: " + e.getMessage());
                JOptionPane.showMessageDialog(this.frame, "CRITICAL FAILURE: " + e.getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
                frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                AllowExit();
                return;
            }
        }

        // overwrite assets with custom ones from each mod and/or perform operations as specified in mod's delete list
        // todo: implement RegEx functions after delete.txt
        for (Main.Mod m : Main.Mods) {
            if (m.enabled) {
                try {
                    System.out.println("Firestar is extracting " + m.friendlyName + " by " + m.author);
                    consoleDisplay.append("Firestar is extracting " + m.friendlyName + " by " + m.author + "\n");
                    new ZipFile(System.getProperty("user.home") + "/.firestar/mods/" + m.path.trim()).extractAll(System.getProperty("user.home") + "/.firestar/temp/");

                    if (new File(System.getProperty("user.home") + "/.firestar/temp/delete.txt").isFile()) {
                        System.out.println("Firestar is deleting files that conflict with " + m.friendlyName + " by " + m.author);
                        consoleDisplay.append("Firestar is deleting files that conflict with " + m.friendlyName + " by " + m.author + "\n");

                        String deleteQueue = new String(Files.readAllBytes(Paths.get(System.getProperty("user.home") + "/.firestar/temp/delete.txt")));
                        if (Main.windows) {deleteQueue = new String(Files.readAllBytes(Paths.get(System.getProperty("user.home") + "\\.firestar\\temp\\delete.txt")));} // might be unnecessary
                        String[] dQarray = deleteQueue.split("\n");
                        Arrays.sort(dQarray);
                        System.out.println("The deletion queue is " + dQarray.length + " files long!"); //debug

                        for (String file : dQarray) {
                            if(file.contains("..")) { //todo: find all possible hazardous paths and blacklist them with regex
                                System.out.println("WARNING: Firestar skipped a potentially dangerous delete command. Please ensure the mod you're installing is from someone you trust!");
                                consoleDisplay.append("WARNING: Firestar skipped a potentially dangerous delete command. Please ensure the mod you're installing is from someone you trust!\n");
                            } else {
                                if (!Main.windows) {
                                    System.out.println("Deleting " + System.getProperty("user.home") + "/.firestar/temp/data/" + file);
                                    consoleDisplay.append("Deleting " + System.getProperty("user.home") + "/.firestar/temp/data/" + file + "\n");
                                    new File(System.getProperty("user.home") + "/.firestar/temp/data" + file).delete();}
                                else {
                                    System.out.println("Deleting " + new String(System.getProperty("user.home") + "\\.firestar\\temp\\data" + file).replace("/", "\\"));
                                    consoleDisplay.append("Deleting " + new String(System.getProperty("user.home") + "\\.firestar\\temp\\data" + file).replace("/", "\\") + "\n");
                                    new File(new String(System.getProperty("user.home") + "\\.firestar\\temp\\data" + file).replace("/", "\\")).delete();
                                }
                            }
                        }

                        // cleanup so we don't run it again for the next mod unless needed
                        // this is not necessary but good practice
                        new File(System.getProperty("user.home") + "/.firestar/temp/delete.txt").delete();
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    consoleDisplay.append("CRITICAL FAILURE: " + e.getMessage());
                    JOptionPane.showMessageDialog(this.frame, "CRITICAL FAILURE: " + e.getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
                    frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                    AllowExit();
                    return;
                }
            }
        }

        // create a list of the contents of data/ for psp2psarc.exe to read from
        List<String> oFilesList = new ArrayList<String>();
        List<String> oFilesList2 = new ArrayList<String>();
        try {
            listAllFiles(Paths.get(System.getProperty("user.home") + "/.firestar/temp/data/"), oFilesList);
            for (String p : oFilesList) {
                // We need to clean up the path here on Linux to avoid psp2psarc getting confused about where the hell "/" is.
                // In WINE it should see it as Z: by default, but if it's somewhere else then I don't have an elegant way of knowing what drive letter it's on, so
                // relative paths are kind of the only choice here. This can be extended to Windows too as it works there, though completely unnecessary.
                if (!Main.windows) {oFilesList2.add(p.replace("\\", "/").split(new String(System.getProperty("user.home") + "/.firestar/temp/"))[1]);}
                else {oFilesList2.add(p.split(new String(System.getProperty("user.home") + "\\.firestar\\temp\\").replace("\\", "\\\\"))[1]);} // path wont match regex unless adjusted for windows here
            }
            //oFilesList2.forEach(System.out::println); //debug
            File oFilesListO = new File(System.getProperty("user.home") + "/.firestar/temp/list.txt");
            if (oFilesListO.isFile()) {oFilesListO.delete();}
            FileWriter oFilesListWr = new FileWriter(oFilesListO, true);
            int i = 0;
            for (String p : oFilesList2) {
                oFilesListWr.append(p);
                if (i != oFilesList2.size()) {
                    oFilesListWr.append("\n");
                }
                i++;
            }
            oFilesListWr.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            consoleDisplay.append("CRITICAL FAILURE: " + e.getMessage());
            JOptionPane.showMessageDialog(this.frame, "CRITICAL FAILURE: " + e.getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
            frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            AllowExit();
            return;
        }

        // invoke psp2psarc.exe one final time to reconstruct the assets
        try {
            System.out.println("Firestar is compiling the final build");
            consoleDisplay.append("Firestar is compiling the final build" + "\n");
            Process p;
            if (!Main.windows) {p = Runtime.getRuntime().exec(new String[]{"bash","-c","cd " + System.getProperty("user.home") + "/.firestar/temp" + ";wine ../psp2psarc.exe create --skip-missing-files -j12 -a -i --input-file=list.txt -o " + oArcTarget});}
            else {p = Runtime.getRuntime().exec(new String[]{new String(System.getProperty("user.home") + "\\.firestar\\psp2psarc.exe"), "create", "--skip-missing-files", "-j12", "-a", "-i", "--input-file=list.txt", "-o" + oArcTarget}, null, new File(new String(System.getProperty("user.home") + "/.firestar/temp/").replace("/", "\\")));}
            final Thread ioThread = new Thread() {
                @Override
                public void run() {
                    try {
                        final BufferedReader reader = new BufferedReader(
                                new InputStreamReader(p.getInputStream()));
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            System.out.println(line);
                            consoleDisplay.append(line + "\n");
                            try {scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());}
                            catch (Exception e) {System.out.println("WARNING: Swing failed to paint window due to race condition.\n" + e.getMessage());}
                        }
                        reader.close();
                    } catch (final Exception e) {
                        e.printStackTrace(); // will probably definitely absolutely for sure hang firestar unless we do something. Too bad!
                    }
                }
            };
            ioThread.start();
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
            consoleDisplay.append("CRITICAL FAILURE: " + e.getMessage());
            JOptionPane.showMessageDialog(this.frame, "CRITICAL FAILURE: " + e.getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
            frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            AllowExit();
            return;
        }

        // cleanup
        boolean one = new File(Main.outpath).mkdirs();
        boolean two;
        System.out.println("created export folder: " + one);
        if (!Main.windows) {two = new File(System.getProperty("user.home") + "/.firestar/temp/" + oArcTarget).renameTo(new File(Main.outpath + oArcTarget));}
        else {two = new File(System.getProperty("user.home") + "\\.firestar\\temp\\" + oArcTarget).renameTo(new File(Main.outpath + oArcTarget));}
        System.out.println("moved file to destination: " + two);
        if (two) {System.out.println("file should be located at " + Main.outpath + oArcTarget);} else {
            System.out.println("CRITICAL FAILURE: Please check that your output path is correct and that you have write permissions!");
            consoleDisplay.append("CRITICAL FAILURE: Please check that your output path is correct and that you have write permissions!");
            JOptionPane.showMessageDialog(this.frame, "CRITICAL FAILURE: Please check that your output path is correct and that you have write permissions!", "Fatal Error", JOptionPane.ERROR_MESSAGE);
            frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            AllowExit();
            return;
        }
        try {
            Process p;
            if (!Main.windows) {p = Runtime.getRuntime().exec(new String[]{"bash","-c","rm -rf " + System.getProperty("user.home") + "/.firestar/temp/"});} // Scary!
            else {p = Runtime.getRuntime().exec(new String[]{"rmdir", new String(System.getProperty("user.home") + "/.firestar/temp/").replace("/", "\\").replace("\\", "\\\\"), "/s", "/q"});}
            //new File(System.getProperty("user.home") + "/.firestar/temp/").delete();
        } catch (IOException e) {
            System.out.println("WARNING: Temporary files may not have been properly cleared.\n" + e.getMessage());
            consoleDisplay.append("WARNING: Temporary files may not have been properly cleared.\n" + e.getMessage());
        }

        // done!
        try {
            TimeUnit.SECONDS.sleep(1); // avoid race condition when logging
        } catch (InterruptedException e) {
            //ignore
        }

        TitledBorder titledBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "DONE! Close this pop-up to continue.");
        titledBorder.setTitlePosition(TitledBorder.BOTTOM);
        titledBorder.setTitleJustification(TitledBorder.CENTER);
        scrollPane.setBorder(titledBorder);
        scrollPane.repaint();

        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        AllowExit();
    }

    private void FastRoutine() {

    }

    public void AllowExit() {
        System.out.println("\n\nYou may now close the pop-up window.");
        consoleDisplay.append("\n\n\nYou may now close the pop-up window.");
        try {TimeUnit.MILLISECONDS.sleep(200);} catch (InterruptedException e) {} //ignore
        scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e)
            {
                invoker.wrapUpDeployment();
                e.getWindow().dispose();
            }
        });
    }

    private static void listAllFiles(Path currentPath, List<String> allFiles)
            throws IOException
    {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentPath))
        {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    listAllFiles(entry, allFiles);
                } else {
                    allFiles.add(entry.toString());
                }
            }
        }
    }
}
