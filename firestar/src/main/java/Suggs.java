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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Suggs implements ActionListener, ListSelectionListener {
    private BufferedImage windowIcon;
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

        try {
            windowIcon = ImageIO.read(Main.class.getResourceAsStream("/titleIcon.png"));
            frame.setIconImage(windowIcon);
        } catch (IOException e) {
            System.out.println("ERROR: Failed to find /resources/titleIcon.png. Window will not have an icon.");
        }
        frame.add(frameContainer); // initialize window contents -- will be handled by IntelliJ IDEA

        frame.setSize(700, 400);
        frame.setMinimumSize(new Dimension(700,400));
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
    
    private JFrame progressFrame = new JFrame();
    public JProgressBar progressBar;
    private JPanel progressFrameContainer;
    private JLabel progressLabel;
    
    private void save() {
	progressFrameContainer = new JPanel();
	
	progressLabel = new JLabel("Generating audio files...");
	progressBar = new JProgressBar();
	progressBar.setMaximum(tracklist.size());
	
	progressFrameContainer.add(progressLabel);
	progressFrameContainer.add(progressBar);
	
	progressFrame.add(progressFrameContainer);
        progressFrame.setSize(300, 100);
        progressFrame.setTitle("Soundtrack Mod Generator");
        progressFrame.setResizable(false);
        progressFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        progressFrame.setLayout(new GridLayout());
        progressFrame.setLocationRelativeTo(null);
        progressFrame.setAlwaysOnTop(true);
        progressFrame.setIconImage(windowIcon);
	
	// Lord, forgive me for this style hell
	progressFrameContainer.setBackground(new Color(-15128227));
	progressFrameContainer.setForeground(new Color(-1));
	progressBar.setBackground(new Color(-1));
	progressBar.setForeground(new Color(-2271221));
	progressLabel.setBackground(new Color(-15128227));
	progressLabel.setForeground(new Color(-1));
	
        progressFrame.setVisible(true);
	
	frame.dispose();
	
	new File(System.getProperty("user.home") + "/.firestar/temp/audio/music").mkdirs();
	
	int i = 0;
	for (AudioTrack at : tracklist) {
	    Process p;
	    try {
		System.out.println("Encoding \"" + at.title + " - " + at.artist + "\"...");
		new File(System.getProperty("user.home") + "/.firestar/temp/audio/music/" + String.format("%02d", i)).mkdirs();
		if (!Main.windows) {p = Runtime.getRuntime().exec(new String[]{"bash","-c","cd " + System.getProperty("user.home") + "/.firestar/temp/" + ";wine ../at9tool.exe -e -br 144 \"" + at.path + "\" audio/music/" + String.format("%02d", i) + "/music_stereo.at9"});}
		else {p = Runtime.getRuntime().exec(new String[]{(System.getProperty("user.home") + "\\.firestar\\at9tool.exe"), "-e", "-br", "144", at.path, "audio\\music\\" + String.format("%02d", i) + "\\music_stereo.at9"}, null, new File((System.getProperty("user.home") + "/.firestar/temp/").replace("/", "\\")));}
		p.waitFor();
	    } catch (IOException | InterruptedException ex) {
		Logger.getLogger(Suggs.class.getName()).log(Level.SEVERE, null, ex);
	    }
	    progressBar.setValue(i++);
	}
	System.out.println("Finished encoding.");
	progressFrame.dispose();
	parent.setEnabled(true);
    }
}
