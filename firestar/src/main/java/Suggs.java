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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
    private JLabel dSTitleLabel;
    private JLabel dMTitleLabel;
    private JLabel dSTitle;
    private JLabel dMTitle;
    private JCheckBox checkAdditive;
    private Scooter progressDialog;
    
    JFrame parent;
    int curIndex = -1;
    
    public class AudioTrack {
        public String path; // file name
        public String title; // audio title
	public String artist; // audio artist
	public int index; // track number
	public int size; // file size in bytes
	public boolean noConvert;
    }
    private static List<AudioTrack> tracklist = new ArrayList<AudioTrack>();
    private String sptrack;
    private String mptrack;

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
    public void valueChanged(ListSelectionEvent listSelectionEvent) { // TODO: change fields on form, show file size, and show MT_(track number) when selection changed.
	curIndex = dSongList.getSelectedIndex();
	System.out.println(curIndex);
	if (curIndex >= 0) {
	    fTitle.setText(tracklist.get(curIndex).title);
	    fArtist.setText(tracklist.get(curIndex).artist);
	} else {
	    fTitle.setText("");
	    fArtist.setText("");
	}
    }
    
    private void remove(int index) {
        if (index > 0) {
            tracklist.remove(curIndex);
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
            {tracklist.get(i).title = new File(tracklist.get(i).path).getName();}
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
	    sptrack = selectedFile.getPath();
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
	    mptrack = selectedFile.getPath();
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
		    track.path = f.getPath();
		    track.title = f.getName();
		    track.index = tracklist.size();
		    tracklist.add(track);
		}
	    }
            InitializeSongListInGUI();
        }
    }
    
    private void save() {
	frame.setEnabled(false);
	frame.setAlwaysOnTop(false);
	Main.deleteDir(new File(System.getProperty("user.home") + "/.firestar/temp/")); // starts with clean temp
	new Thread(() -> {
	    int progressSize = tracklist.size()+(sptrack != null?1:0)+(mptrack != null?1:0)+2; // Accounting for processes

	    progressDialog = new Scooter();
	    progressDialog.showDialog("Soundtrack Mod Generator");
	    progressDialog.setText("Generating audio files...");
	    progressDialog.setProgressMax(progressSize);
	    
	    new File(System.getProperty("user.home") + "/.firestar/temp/data/audio/music").mkdirs();
	    
	    for (int i = 0; i < tracklist.size(); i++) {
		AudioTrack at = tracklist.get(i);
		String trackno = String.format("%02d", i);
		progressDialog.setText("Encoding track " + (i+1) + " out of " + tracklist.size() + "...");
		try {
		    System.out.println("Encoding track #" + (i+1) + " \"" + at.title + " - " + at.artist + "\"...");
		    new File(System.getProperty("user.home") + "/.firestar/temp/data/audio/music/" + trackno).mkdirs();
		    Process p = Main.exec(new String[]{"../at9tool.exe", "-e", "-br", "144", at.path, "data/audio/music/" + trackno + "/music_stereo.at9"}, System.getProperty("user.home") + "/.firestar/temp/");
		    p.waitFor();
		} catch (IOException | InterruptedException ex) {
		    Logger.getLogger(Suggs.class.getName()).log(Level.SEVERE, null, ex);
		}
		progressDialog.setProgressValue(i);
	    }
	    if (sptrack != null) {
		progressDialog.setText("Encoding singleplayer frontend track...");
		if (new File(sptrack).exists()) {
		    try {
			System.out.println("Encoding singleplayer frontend track...");
			new File(System.getProperty("user.home") + "/.firestar/temp/data/audio/music/FEMusic").mkdirs();
			Process p = Main.exec(new String[]{"../at9tool.exe", "-e", "-br", "144", sptrack, "data/audio/music/FEMusic/frontend_stereo.at9"}, System.getProperty("user.home") + "/.firestar/temp/");
			p.waitFor();
		    } catch (IOException | InterruptedException ex) {
			Logger.getLogger(Suggs.class.getName()).log(Level.SEVERE, null, ex);
		    }
		}
		progressDialog.setProgressValue(progressDialog.getProgressValue()+1);
	    }
	    if (mptrack != null) {
		progressDialog.setText("Encoding multiplayer frontend track...");
		try {
		    assert(new File(mptrack).exists());
		    System.out.println("Encoding multiplayer frontend track...");
		    new File(System.getProperty("user.home") + "/.firestar/temp/data/audio/music/FEDemoMusic").mkdirs();
		    Process p = Main.exec(new String[]{"../at9tool.exe", "-e", "-br", "144", mptrack, "data/audio/music/FEDemoMusic/frontend_stereo.at9"}, System.getProperty("user.home") + "/.firestar/temp/");
		    p.waitFor();
		} catch (IOException | InterruptedException ex) {
		    Logger.getLogger(Suggs.class.getName()).log(Level.SEVERE, null, ex);
		}
		progressDialog.setProgressValue(progressDialog.getProgressValue()+1);
	    }
	    System.out.println("Finished encoding.");
	    
	    progressDialog.setText("Generating Music Definitions...");
	    /*
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    try {
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new File());
		NodeList screen = doc.getElementsByTagName("Screen");
		for (int i = 0; i < tracklist.size(); i++) {
		    AudioTrack at = tracklist.get(i);
		    Node node = screen.item(i);
		}
	    } catch (ParserConfigurationException | SAXException | IOException e) {
		e.printStackTrace();
	    }*/
	    progressDialog.setProgressValue(progressDialog.getProgressValue()+1);
	    
	    progressDialog.destroyDialog();
	    frame.dispose();
	    new Clifford().Action(frame, new File(System.getProperty("user.home") + "/.firestar/temp/"));
	    System.out.println("Post Clifford");
	    parent.setEnabled(true);
	}).start();
    }
}
