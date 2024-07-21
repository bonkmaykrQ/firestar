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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class Suggs implements ActionListener, ListSelectionListener {
    public JFrame frame = new JFrame();
    private JPanel frameContainer;
    private JList dSongList;
    private JButton moveDownBtn;
    private JButton saveBtn;
    private JButton cancelBtn;
    private JTextField fTitle;
    private JTextField fArtist;
    private JLabel dTrackNo;
    private JLabel dFileSize;
    private JButton frontendDemoChooseBtn;
    private JButton frontendMainChooseBtn;
    private JButton deleteSongBtn;
    private JButton addSongBtn;
    private JButton moveUpBtn;
    private JLabel dSTitle;
    private JLabel dMTitle;
    private JLabel dSSize;
    private JLabel dMSize;
    private JCheckBox checkAdditive;
    private Scooter progressDialog;
    
    JFrame parent;
    int curIndex = -1;
    
    public class AudioTrack {
        public File path; // file name
        public String title; // audio title
	public String artist; // audio artist
	public long size; // file size in bytes
	public boolean noConvert;
    }
    private static List<AudioTrack> tracklist = new ArrayList<AudioTrack>();
    private File sptrack;
    private File mptrack;

    public Suggs(JFrame parent) {
	this.parent = parent;
        parent.setEnabled(false);

        frame.setIconImage(Main.windowIcon);
        frame.add(frameContainer); // initialize window contents -- will be handled by IntelliJ IDEA

        frame.setSize(700, 400);
        frame.setMinimumSize(new Dimension(650,280));
        frame.setTitle("Soundtrack Mod Generator");
        frame.setResizable(true);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setLayout(new GridLayout());
        frame.setLocationRelativeTo(parent);
        frame.setAlwaysOnTop(true);

        cancelBtn.addActionListener(this); // TODO: put warning dialog "Are you sure? All unsaved changes will be lost."
        saveBtn.addActionListener(this);
        addSongBtn.addActionListener(this);     // file picker
        deleteSongBtn.addActionListener(this);  // delete from list
        moveUpBtn.addActionListener(this);
        moveDownBtn.addActionListener(this);
        fTitle.addActionListener(this);         // automatically change selected item when changed &
        fArtist.addActionListener(this);        // also update field when new item selected
        frontendMainChooseBtn.addActionListener(this);      // file picker for singleplayer campaign grid music
        frontendDemoChooseBtn.addActionListener(this);      // file picker for multiplayer lobby music
        dSongList.addListSelectionListener(this);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e)
            {// TODO: put warning dialog "Are you sure? All unsaved changes will be lost."
                parent.setEnabled(true);
                e.getWindow().dispose();
            }
        });

        frame.setVisible(true);
    }

    private void haveSeggs() { // kill yourself

    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
	if (actionEvent.getSource() == cancelBtn) {
	    parent.setEnabled(true);
            frame.dispose();
	} else
	if (actionEvent.getSource() == fTitle || actionEvent.getSource() == fArtist) {
	    tracklist.get(curIndex).title = fTitle.getText();
	    tracklist.get(curIndex).artist = fArtist.getText();
	    InitializeSongListInGUI();
	    dSongList.setSelectedIndex(curIndex);
	} else
	if (actionEvent.getSource() == addSongBtn) {addSong();} else
	if (actionEvent.getSource() == deleteSongBtn) {remove(curIndex);} else
	if (actionEvent.getSource() == moveUpBtn) {moveUp(curIndex);} else
	if (actionEvent.getSource() == moveDownBtn) {moveDown(curIndex);} else
	if (actionEvent.getSource() == frontendMainChooseBtn) {setSPMusic();} else 
	if (actionEvent.getSource() == frontendDemoChooseBtn) {setMPMusic();} else
	if (actionEvent.getSource() == saveBtn) {save();}
    }

    @Override
    public void valueChanged(ListSelectionEvent listSelectionEvent) {
	curIndex = dSongList.getSelectedIndex();
	if (curIndex >= 0) {
	    AudioTrack at = tracklist.get(curIndex);
	    fTitle.setText(at.title);
	    fArtist.setText(at.artist);
	    dTrackNo.setText(String.format("MT_%02d", curIndex+1));
	    dFileSize.setText((at.size / 1000) + "kb");
	} else {
	    fTitle.setText("");
	    fArtist.setText("");
	    dTrackNo.setText("--");
	    dFileSize.setText("-kb");
	}
    }
    
    private void remove(int index) {
        if (index >= 0) {
            tracklist.remove(index);
            InitializeSongListInGUI();
        }
    }
    
    private void moveUp(int index) {
        if (index > 0) {
            Collections.swap(tracklist, index, index - 1);
            InitializeSongListInGUI();
        }
    }

    private void moveDown(int index) {
        if (index < (tracklist.size() - 1)) {
            Collections.swap(tracklist, index, index + 1);
            InitializeSongListInGUI();
        }
    }
    
    private void InitializeSongListInGUI() {
        dSongList.clearSelection();
        dSongList.removeAll();
        dSongList.setVisibleRowCount(tracklist.size());
        dSongList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // add text entry for each
        int i = 0;
        String[] contents = new String[tracklist.size()];
        while (i < tracklist.size()) {
            if (tracklist.get(i).title == null || tracklist.get(i).title.isEmpty())
            {tracklist.get(i).title = tracklist.get(i).path.getName();}
	    if (tracklist.get(i).artist == null || tracklist.get(i).artist.isEmpty())
            {tracklist.get(i).artist = "???";}
            contents[i] = tracklist.get(i).artist + " - " + tracklist.get(i).title;

            i++;
        }
        dSongList.setListData(contents);
	dSongList.setSelectedIndex(curIndex);
    }
    
    private void setSPMusic() {
	JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("WAVE", "wav"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("ATRAC9", "at9"));

        int result = fileChooser.showOpenDialog(frame);
	if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
	    sptrack = selectedFile;
	    dSTitle.setText(selectedFile.getName());
	    dSSize.setText((selectedFile.length() / 1000) + "kb");
        }
    }
    
    private void setMPMusic() {
	JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("WAVE", "wav"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("ATRAC9", "at9"));

        int result = fileChooser.showOpenDialog(frame);
	if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
	    mptrack = selectedFile;
	    dMTitle.setText(selectedFile.getName());
	    dMSize.setText((selectedFile.length() / 1000) + "kb");
        }
    }
    
    private void addSong() {
	JFileChooser fileChooser = new JFileChooser();
	fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("WAVE", "wav"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("ATRAC9", "at9"));

        int result = fileChooser.showOpenDialog(frame);
	if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
	    for (File f : selectedFiles) {
		if (f.exists()) {
		    System.out.println("Importing audio file \"" + f.getName() + "\"");
		    AudioTrack track = new AudioTrack();
		    track.path = f;
		    String fname = f.getName();
		    if (fname.contains(" - ")) {
			track.title  = fname.substring(fname.indexOf(" - ")+3, fname.lastIndexOf("."));
			track.artist  = fname.substring(0, fname.indexOf(" - "));
		    } else track.title = fname;
		    track.size = f.length();
		    tracklist.add(track);
		}
	    }
            InitializeSongListInGUI();
        }
    }
    
    private void save() {
		if (tracklist.isEmpty() && sptrack == null && mptrack == null) {
			JOptionPane.showMessageDialog(frame, "Please add at least one song to the playlist.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

	frame.setEnabled(false);
	frame.setAlwaysOnTop(false);
	Main.deleteDir(new File(System.getProperty("user.home") + "/.firestar/temp/")); // starts with clean temp
	new Thread(() -> {
	    int progressSize = tracklist.size()+(sptrack != null?1:0)+(mptrack != null?1:0)+3; // Accounting for processes

	    progressDialog = new Scooter();
	    progressDialog.showDialog("Soundtrack Mod Generator");
	    progressDialog.setText("Generating audio files...");
	    progressDialog.setProgressMax(progressSize);
	    progressDialog.setProgressValue(0);
	    
	    try {
		new File(Main.inpath + "temp/data/audio/music").mkdirs();
		FileOutputStream fos = new FileOutputStream(new File(Main.inpath + "temp/fscript"));
		PrintStream ps = new PrintStream(fos);
		ps.println("fscript 1");
		ps.println("# AUTOGENERATED BY FIRESTAR");
		for (int i = 0; i < tracklist.size(); i++) {
		    AudioTrack at = tracklist.get(i);
		    String trackno = String.format("%02d", i+1);
		    new File(Main.inpath + "temp/data/audio/music/" + trackno).mkdirs();
		    if (at.path.getName().endsWith(".at9")) {
			progressDialog.setText("Copying track " + (i+1) + " out of " + tracklist.size() + "...");
			try {
			    // Assume whoever made the AT9s knows what they're doing
			    System.out.println("Copying track #" + (i+1) + " \"" + at.artist + " - " + at.title + "\"...");
			    Files.copy(at.path.toPath(), Paths.get(Main.inpath + "tmp/data/audio/music/" + trackno + "/music_stereo.at9"), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException ex) {
			    Logger.getLogger(Suggs.class.getName()).log(Level.SEVERE, null, ex);
			}
		    } else {
			progressDialog.setText("Encoding track " + (i+1) + " out of " + tracklist.size() + "...");
			try {
			    System.out.println("Encoding track #" + (i+1) + " \"" + at.artist + " - " + at.title + "\"...");
			    Process p = Main.exec(new String[]{Main.inpath + "at9tool.exe", "-e", "-br", "144", at.path.getPath(), "data/audio/music/" + trackno + "/music_stereo.at9"}, Main.inpath + "temp/");
			    p.waitFor();
			} catch (IOException | InterruptedException ex) {
			    Logger.getLogger(Suggs.class.getName()).log(Level.SEVERE, null, ex);
			}
		    }
		    String ocmd = "modify";
		    if (i >= 12) ocmd = "create";
		    ps.println("file \"data/plugins/languages/american/entries.xml\" xml "+ocmd+" StringTable.entry#MT_"+trackno+" set attribute \"string\" \""+at.artist.replace("\"", "\\\"")+"\\n"+at.title.replace("\"", "\\\"")+"\"");
		    ps.println("file \"data/audio/music/"+trackno+"/music_stereo.fft\" delete");
		    progressDialog.setProgressValue(i+1);
		}
		for (int s = tracklist.size(); s < 12; s++) {
		    ps.println("file \"data/audio/music/"+String.format("%02d", s+1)+"/music_stereo.fft\" delete");
		}
		if (sptrack != null) {
		    progressDialog.setText("Encoding singleplayer frontend track...");
		    if (sptrack.exists()) {
			try {
			    System.out.println("Encoding singleplayer frontend track...");
			    new File(Main.inpath + "temp/data/audio/music/FEMusic").mkdirs();
			    Process p = Main.exec(new String[]{Main.inpath + "at9tool.exe", "-e", "-br", "144", sptrack.getPath(), "data/audio/music/FEMusic/frontend_stereo.at9"}, System.getProperty("user.home") + "/.firestar/temp/");
			    p.waitFor();
			} catch (IOException | InterruptedException ex) {
			    Logger.getLogger(Suggs.class.getName()).log(Level.SEVERE, null, ex);
			}
		    }
		    ps.println("file \"data/audio/music/FEMusic/frontend_stereo.fft\" delete");
		    progressDialog.setProgressValue(progressDialog.getProgressValue()+1);
		}
		if (mptrack != null) {
		    progressDialog.setText("Encoding multiplayer frontend track...");
		    try {
			assert(mptrack.exists());
			System.out.println("Encoding multiplayer frontend track...");
			new File(Main.inpath + "temp/data/audio/music/FEDemoMusic").mkdirs();
			Process p = Main.exec(new String[]{Main.inpath + "at9tool.exe", "-e", "-br", "144", mptrack.getPath(), "data/audio/music/FEDemoMusic/frontend_stereo.at9"}, System.getProperty("user.home") + "/.firestar/temp/");
			p.waitFor();
		    } catch (IOException | InterruptedException ex) {
			Logger.getLogger(Suggs.class.getName()).log(Level.SEVERE, null, ex);
		    }
		    ps.println("file \"data/audio/music/FEDemoMusic/frontend_stereo.fft\" delete");
		    progressDialog.setProgressValue(progressDialog.getProgressValue()+1);
		}
		System.out.println("Finished encoding.");

		progressDialog.setText("Generating Music Definitions...");
		try {
		    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		    Document defDoc = docBuilder.newDocument();
		    Element docScreen = defDoc.createElement("Screen");
		    docScreen.setAttribute("name", "Top");
		    for (int i = 0; i < tracklist.size(); i++) { // TODO: support for additive
			AudioTrack at = tracklist.get(i);
			String trackno = String.format("%02d", i+1);

			Element trackElem = defDoc.createElement("PI_Music");
			trackElem.setAttribute("name", trackno);

			Element pathElem = defDoc.createElement("Values");
			pathElem.setAttribute("location", "data\\audio\\music\\"+trackno);
			trackElem.appendChild(pathElem);

			Element artistElem = defDoc.createElement("Entry");
			artistElem.setAttribute("Artist", at.artist);
			trackElem.appendChild(artistElem);

			Element titleElem = defDoc.createElement("Entry");
			titleElem.setAttribute("Label", at.title);
			trackElem.appendChild(titleElem);

			docScreen.appendChild(trackElem);
			System.out.println("Adding \"" + trackno + ". " + at.artist + " - " + at.title + "\" to Definition.xml");
		    }
		    defDoc.appendChild(docScreen);

		    new File(Main.inpath + "temp/data/plugins/music/").mkdirs();
		    FileOutputStream output = new FileOutputStream(Main.inpath + "temp/data/plugins/music/Definition.xml");
		    TransformerFactory transformerFactory = TransformerFactory.newInstance();
		    Transformer transformer = transformerFactory.newTransformer();
		    DOMSource source = new DOMSource(defDoc);
		    StreamResult result = new StreamResult(output);

		    transformer.transform(source, result);
		} catch (IOException | ParserConfigurationException | TransformerException ex) {
		    Logger.getLogger(Suggs.class.getName()).log(Level.SEVERE, null, ex);
		}
		progressDialog.setProgressValue(progressDialog.getProgressValue()+1);

		progressDialog.setText("Finalizing script...");
		System.out.println("Finalizing Fscript...");
		ps.println("# END FIRESTAR AUTOGENERATION");
		ps.close();
		fos.close();
	    } catch (IOException ex) {
		Logger.getLogger(Suggs.class.getName()).log(Level.SEVERE, null, ex);
	    }
	    
	    progressDialog.setProgressValue(progressDialog.getProgressValue()+1);
	    
	    progressDialog.destroyDialog();
	    frame.dispose();
	    Clifford saveDialog = new Clifford();
		saveDialog.isSoundtrack = true;
		saveDialog.Action(frame, new File(Main.inpath + "temp/"));
	    parent.setEnabled(true);
	}).start();
    }
}
