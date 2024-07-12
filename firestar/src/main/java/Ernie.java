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

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Ernie implements ActionListener, Runnable {
    // Base URL for Gitea API
    public static final String gitapi = "https://git.worlio.com/api/v1/repos/bonkmaykr/firestar";
    public static final String changelog = "https://bonkmaykr.worlio.com/http/firestar/changelog.htm";
    
    private HttpURLConnection httpConn;
    private int contentLength;
    private InputStream inputStream;
    
    private JFrame frame = new JFrame();
    private JPanel frameContainer;
    private JEditorPane changelogDisplay;
    private JButton notnowbtn;
    private JButton surebtn;
    
    public boolean backgroundDone = false;
    
    public void run() {
	byte[] urlraw = new Ernie().getFile(gitapi+"/releases?draft=false&pre-release=false");
	if (urlraw.length <= 0) {
	   JOptionPane.showMessageDialog(frame, "Internal Error: Couldn't check for updates.", "Fatal Error", JOptionPane.ERROR_MESSAGE);
	} else {
	    try {
		JSONArray releases = new JSONArray(new String(urlraw, Charset.forName("utf-8")));
		if (releases.length() >= 1 && ((JSONObject)releases.get(0)).get("tag_name") != Main.vtag) {
		    frame.add(frameContainer);
		    notnowbtn.addActionListener(this);
		    surebtn.addActionListener(this);
		    changelogDisplay.setEditable(false);
		    changelogDisplay.setPage(changelog);
            changelogDisplay.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE); // makes text smaller than joe biden's windmill if font set manually. wtf??
		    
		    frame.setTitle("An update is available!");
		    frame.setSize(450, 400);
		    frame.setResizable(false);
		    frame.setLocationRelativeTo(null);
		    frame.setAlwaysOnTop(true);
		    frame.setVisible(true);
		} else { System.out.println("Problem??"); }
	    } catch (IOException ex) {
		Logger.getLogger(Ernie.class.getName()).log(Level.SEVERE, null, ex);
	    } catch (JSONException ex) {
		Logger.getLogger(Ernie.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}
    }
    
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == notnowbtn) {frame.dispose();} else
        if (actionEvent.getSource() == surebtn) {
	    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
		String releasepage = "https://git.worlio.com/bonkmaykr/firestar/releases";
		try {
		    Desktop.getDesktop().browse(new URI(releasepage));
            frame.dispose();
		} catch (Exception ex) {
		    JOptionPane.showMessageDialog(frame, "Couldn't open your web browser!\nYou can check out the latest release at the URL below:\n"+releasepage, "Error", JOptionPane.ERROR_MESSAGE);
		}
	    }
	}
    }
    
    byte[] getFile(String url) {
	try {
	    URL fileURL = new URL(url);
	    httpConn = (HttpURLConnection) fileURL.openConnection();
	    int response = httpConn.getResponseCode();

            if (response == HttpURLConnection.HTTP_OK) {
                contentLength = httpConn.getContentLength();
                inputStream = httpConn.getInputStream();
            } else if (response == 404) {
                throw new IOException(
                        "File missing from remote server.");
            } else {
                throw new IOException(
                        "Unexpected response; Server replied with " + response);
            }
	    ErnieDownloader ed = new ErnieDownloader(this, url, httpConn.getContentLength());
	    ed.doInBackground();
            while (!backgroundDone) {}

            inputStream.close();
            httpConn.disconnect();
            frame.dispose();
            return ed.output;
	} catch (MalformedURLException e) {
            JOptionPane.showMessageDialog(frame, "Internal Error: URL given to Ernie is not valid.\nGet a programmer!", "Fatal Error", JOptionPane.ERROR_MESSAGE);
            frame.dispose();
            return new byte[]{};
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
            frame.dispose();
            return new byte[]{};
        }
    }
}

class ErnieDownloader extends SwingWorker<Void, Void> {
    private static final int BUFFER_SIZE = 4096;
    private String downloadURL;
    private final Ernie gui;
    public byte[] output;

    public ErnieDownloader(Ernie gui, String downloadURL, long size) {
        this.gui = gui;
        this.downloadURL = downloadURL;
    }

    @Override
    protected Void doInBackground() throws Exception {
        BufferedInputStream in = new BufferedInputStream(new URL(downloadURL).openStream());
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	byte[] data = new byte[16384];
	int x;
	while ((x = in.read(data, 0, data.length)) != -1) {
	    buffer.write(data, 0, x);
	}
	output = buffer.toByteArray();
        in.close();
        gui.backgroundDone = true;
        return null;
    }
}