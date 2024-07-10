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
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Fozzie {
    BufferedImage windowIcon;
    private JFrame frame = new JFrame();
    public JProgressBar progressBar;
    private JPanel frameContainer;
    private JLabel label;

    private HttpURLConnection httpConn;
    private int contentLength;
    private InputStream inputStream;

    public boolean backgroundDone = false;

    boolean DownloadFile(String url, String odir, String oname) {
        frame.add(frameContainer);
        frame.setSize(300, 100);
        frame.setTitle("Download in Progress");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setLayout(new GridLayout());
        frame.setLocationRelativeTo(null);
        frame.setAlwaysOnTop(true);
        try {
            windowIcon = ImageIO.read(Main.class.getResourceAsStream("/titleIcon.png"));
            frame.setIconImage(windowIcon);
        } catch (IOException e) {
            System.out.println("ERROR: Failed to find /resources/titleIcon.png. Window will not have an icon.");
        }
        frame.setVisible(true);

        label.setText("Downloading \"" + oname + "\"");

        try {
            URL fileURL = new URL(url);
            httpConn = (HttpURLConnection) fileURL.openConnection();
            int response = httpConn.getResponseCode();

            if (response == HttpURLConnection.HTTP_OK) {
                String disposition = httpConn.getHeaderField("Content-Disposition");
                String contentType = httpConn.getContentType();
                contentLength = httpConn.getContentLength();

                inputStream = httpConn.getInputStream();
            } else if (response == 404) {
                throw new IOException(
                        "File missing from remote server.");
            } else {
                throw new IOException(
                        "Unexpected response; Server replied with " + response);
            }
            new FozzieDownloader(this, url, odir, oname, httpConn.getContentLength()).doInBackground();
            while (!backgroundDone) {}

            inputStream.close();
            httpConn.disconnect();
            frame.dispose();
            return true;
        } catch (MalformedURLException e) {
            JOptionPane.showMessageDialog(frame, "Internal Error: URL given to Fozzie is not valid.\nGet a programmer!", "Fatal Error", JOptionPane.ERROR_MESSAGE);
            frame.dispose();
            return false;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
            frame.dispose();
            return false;
        }
    }

    public void disconnect() throws IOException {
        inputStream.close();
        httpConn.disconnect();
    }

    public int getContentLength() {
        return this.contentLength;
    }

    public InputStream getInputStream() {
        return this.inputStream;
    }
}

class FozzieDownloader extends SwingWorker<Void, Void> {
    private static final int BUFFER_SIZE = 4096;
    private String downloadURL;
    private String saveDirectory;
    private String saveName;
    private final Fozzie gui;
    private final long completeSize;

    public FozzieDownloader(Fozzie gui, String downloadURL, String saveDirectory, String saveName, long size) {
        this.gui = gui;
        this.downloadURL = downloadURL;
        this.saveDirectory = saveDirectory;
        this.saveName = saveName;
        this.completeSize = size;
    }

    @Override
    protected Void doInBackground() throws Exception {
        long downloadedSize = 0;
        File downloadLocationDir = new File(saveDirectory);
        File downloadLocation = new File(saveDirectory + "/" + saveName);
        downloadLocationDir.mkdirs();
        if (!downloadLocation.isFile()) {
            downloadLocation.createNewFile();
        }
        BufferedInputStream in = new BufferedInputStream(new URL(downloadURL).openStream());
        FileOutputStream fos = new FileOutputStream(saveDirectory + "/" + saveName);
        BufferedOutputStream out = new BufferedOutputStream(fos,1024);
        byte[] data = new byte[1024];
        int x = 0;
        while ((x = in.read(data,0,1024))>=0) {
            downloadedSize += x;
            int progress = (int) ((((double)downloadedSize) / ((double)completeSize)) * 100d);
            gui.progressBar.setValue(progress);
            out.write(data, 0, x);
        }
        out.close();
        in.close();
        gui.backgroundDone = true;
        return null;
    }
}
