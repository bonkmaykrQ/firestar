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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
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
	private JButton spDeleteBtn;
	private JButton mpDeleteBtn;
	private JCheckBox checkNormalize;
	private Scooter progressDialog;
	
	JFrame parent;
	int curIndex = -1;
	boolean normalizeVolumes = false;
	
	public class AudioTrack {
		public File path; // file name
		public String title; // audio title
		public String artist; // audio artist
		public long size; // file size in bytes
		public boolean noConvert;
	}
	private List<AudioTrack> tracklist = new ArrayList<AudioTrack>();
	private File sptrack;
	private File mptrack;

	DocumentListener id3TagEditorHandler = new DocumentListener() {
		@Override
		public void insertUpdate(DocumentEvent documentEvent) {
			updateSelectionToMatchTextFields();
		}

		@Override
		public void removeUpdate(DocumentEvent documentEvent) {
			updateSelectionToMatchTextFields();
		}

		@Override
		public void changedUpdate(DocumentEvent documentEvent) {
			updateSelectionToMatchTextFields();
		}
	};

	public Suggs(JFrame parent) {
	this.parent = parent;
		parent.setEnabled(false);

		frame.setIconImage(Main.windowIcon);
		frame.add(frameContainer); // initialize window contents -- will be handled by IntelliJ IDEA

		frame.setSize(700, 400);
		frame.setMinimumSize(new Dimension(700,400));
		frame.setTitle("Soundtrack Mod Generator");
		frame.setResizable(true);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.setLayout(new GridLayout());
		frame.setLocationRelativeTo(parent);
		frame.setAlwaysOnTop(true);

		cancelBtn.addActionListener(this);
		saveBtn.addActionListener(this);
		addSongBtn.addActionListener(this);	 // file picker
		deleteSongBtn.addActionListener(this);  // delete from list
		moveUpBtn.addActionListener(this);
		moveDownBtn.addActionListener(this);
		fTitle.addActionListener(this);		 // automatically change selected item when changed &
		fArtist.addActionListener(this);		// also update field when new item selected
		frontendMainChooseBtn.addActionListener(this);	  // file picker for singleplayer campaign grid music
		frontendDemoChooseBtn.addActionListener(this);	  // file picker for multiplayer lobby music
		spDeleteBtn.addActionListener(this);
		mpDeleteBtn.addActionListener(this);
		dSongList.addListSelectionListener(this);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e)
			{
				if (!tracklist.isEmpty() || sptrack != null || mptrack != null) {
					int result = JOptionPane.showConfirmDialog(frame, "Are you sure?\nAll unsaved changes will be lost.", "Soundtrack Mod Generator", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if (result == JOptionPane.NO_OPTION) {return;}
				}
				parent.setEnabled(true);
				e.getWindow().dispose();
			}
		});

		fTitle.setText("");
		fArtist.setText("");
		dTrackNo.setText("\u200E");
		dFileSize.setText("\u200E");
		fTitle.setEnabled(false);
		fArtist.setEnabled(false);

		fTitle.getDocument().addDocumentListener(id3TagEditorHandler);
		fArtist.getDocument().addDocumentListener(id3TagEditorHandler);

		frame.setVisible(true);
	}

	private void haveSeggs() { // kill yourself

	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		if (actionEvent.getSource() == cancelBtn) {
			if (!tracklist.isEmpty() || sptrack != null || mptrack != null) {
				int result = JOptionPane.showConfirmDialog(frame, "Are you sure?\nAll unsaved changes will be lost.", "Soundtrack Mod Generator", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (result == JOptionPane.NO_OPTION) {return;}
			}
			parent.setEnabled(true);
			frame.dispose();
		} else
		if (actionEvent.getSource() == addSongBtn) {addSong();} else
		if (actionEvent.getSource() == deleteSongBtn) {remove(curIndex);} else
		if (actionEvent.getSource() == moveUpBtn) {moveUp(curIndex);} else
		if (actionEvent.getSource() == moveDownBtn) {moveDown(curIndex);} else
		if (actionEvent.getSource() == frontendMainChooseBtn) {setSPMusic();} else 
		if (actionEvent.getSource() == frontendDemoChooseBtn) {setMPMusic();} else
		if (actionEvent.getSource() == saveBtn) {
			if (tracklist.isEmpty() && sptrack == null && mptrack == null) {
				JOptionPane.showMessageDialog(frame, "Please add at least one song to the playlist.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			} else {
				String deets;
				if (tracklist.isEmpty()) {
					deets = "no songs";
				} else {
					deets = tracklist.size() + " songs";
				}
				if (sptrack != null && mptrack == null) {
					deets = deets + " and a custom campaign menu track";
				} else if (mptrack != null && sptrack == null) {
					deets = deets + " and a custom lobby menu track";
				} else if (mptrack != null && sptrack != null) {
					deets = deets + ", a custom campaign menu track, and a custom lobby menu track";
				}
				int result = JOptionPane.showConfirmDialog(frame, "Your custom playlist with " + deets + " will be generated.\nPress YES to continue, or NO to continue editing.", "Soundtrack Mod Generator", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (result == JOptionPane.YES_OPTION) {
					save();
				}
			}
		} else
		if (actionEvent.getSource() == spDeleteBtn) {
			dSTitle.setText("no track");
			dSSize.setText("no size");
			sptrack = null;
			spDeleteBtn.setVisible(false);
		} else
		if (actionEvent.getSource() == mpDeleteBtn) {
			dMTitle.setText("no track");
			dMSize.setText("no size");
			mptrack = null;
			mpDeleteBtn.setVisible(false);
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent listSelectionEvent) {
		fTitle.getDocument().removeDocumentListener(id3TagEditorHandler);
		fArtist.getDocument().removeDocumentListener(id3TagEditorHandler);
		curIndex = dSongList.getSelectedIndex();
		if (curIndex >= 0) {
			fTitle.setEnabled(true);
			fArtist.setEnabled(true);
			AudioTrack at = tracklist.get(curIndex);
			fTitle.setText(at.title);
			fArtist.setText(at.artist);
			dTrackNo.setText(String.format("MT_%02d", curIndex+1));
			dFileSize.setText(at.size + " B");
			if (at.size > 1023) {
				dFileSize.setText((at.size / 1024) + " KB");
			}
			if (at.size > 1048575) {
				dFileSize.setText((at.size / 1048576) + " MB");
			}
			fTitle.getDocument().addDocumentListener(id3TagEditorHandler);
			fArtist.getDocument().addDocumentListener(id3TagEditorHandler);
		} else {
			fTitle.setEnabled(false);
			fArtist.setEnabled(false);
			fTitle.setText("");
			fArtist.setText("");
			dTrackNo.setText("\u200E");
			dFileSize.setText("\u200E");
			fTitle.getDocument().addDocumentListener(id3TagEditorHandler);
			fArtist.getDocument().addDocumentListener(id3TagEditorHandler);
		}
	}

	private void updateSelectionToMatchTextFields() {
		tracklist.get(curIndex).title = fTitle.getText();
		tracklist.get(curIndex).artist = fArtist.getText();
		InitializeSongListInGUI();
		dSongList.setSelectedIndex(curIndex);
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
			curIndex--;
			InitializeSongListInGUI();
		}
	}

	private void moveDown(int index) {
		if (index < (tracklist.size() - 1)) {
			Collections.swap(tracklist, index, index + 1);
			curIndex++;
			InitializeSongListInGUI();
		}
	}
	
	private void InitializeSongListInGUI() {
		dSongList.removeListSelectionListener(this); // prevent weird bullshit
		dSongList.clearSelection();
		dSongList.removeAll();
		dSongList.setVisibleRowCount(tracklist.size());
		dSongList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// add text entry for each
		int i = 0;
		String[] contents = new String[tracklist.size()];
		while (i < tracklist.size()) {
			String dTitleInList;	// avoid editing the actual list
			String dArtistInList;	// otherwise we cause weird JTextField behavior
			if (tracklist.get(i).title == null || tracklist.get(i).title.isEmpty())
				dTitleInList = tracklist.get(i).path.getName();
			else 
				dTitleInList = tracklist.get(i).title;
			if (tracklist.get(i).artist == null || tracklist.get(i).artist.isEmpty()) 
				dArtistInList = "Unknown Artist";
			else 
				dArtistInList = tracklist.get(i).artist;
			contents[i] = dArtistInList + " - " + dTitleInList;

			i++;
		}
		dSongList.setListData(contents);
		dSongList.setSelectedIndex(curIndex);
		dSongList.addListSelectionListener(this);
	}

	private JFileChooser commonSoundFilePicker(boolean multiselect) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(multiselect);
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		fileChooser.setFileFilter(new FileNameExtensionFilter("All Compatible Files", "mp3", "ogg", "oga", "opus", "m4a", "3gp", "wav", "wave", "aif", "aiff", "aifc", "flac", "at3", "at9"));
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("MPEG Audio Layer 3", "mp3"));
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Ogg Audio", "ogg", "oga", "opus"));
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("AAC or MPEG-4 Audio", "m4a", "3gp"));
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("IBM/Microsoft WAVE", "wav", "wave"));
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Apple AIFF", "aif", "aiff", "aifc"));
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Free Lossless Audio Codec", "flac"));
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Sony ATRAC3", "at3"));
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Sony ATRAC9", "at9"));
		return fileChooser;
	}
	
	private void setSPMusic() {
		JFileChooser fileChooser = commonSoundFilePicker(false);
		int result = fileChooser.showOpenDialog(frame);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			sptrack = selectedFile;
			dSTitle.setText(selectedFile.getName());
			dSSize.setText(selectedFile.length() + " B");
			if (selectedFile.length() > 1023) {
				dSSize.setText((selectedFile.length() / 1024) + " KB");
			}
			if (selectedFile.length() > 1048575) {
				dSSize.setText((selectedFile.length() / 1048576) + " MB");
			}
			spDeleteBtn.setVisible(true);
		}
	}
	
	private void setMPMusic() {
		JFileChooser fileChooser = commonSoundFilePicker(false);
		int result = fileChooser.showOpenDialog(frame);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			mptrack = selectedFile;
			dMTitle.setText(selectedFile.getName());
			dMSize.setText(selectedFile.length() + " B");
			if (selectedFile.length() > 1023) {
				dMSize.setText((selectedFile.length() / 1024) + " KB");
			}
			if (selectedFile.length() > 1048575) {
				dMSize.setText((selectedFile.length() / 1048576) + " MB");
			}
			mpDeleteBtn.setVisible(true);
		}
	}
	
	private void addSong() {
		JFileChooser fileChooser = commonSoundFilePicker(true);

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
		frame.setEnabled(false);
		frame.setAlwaysOnTop(false);
		normalizeVolumes = checkNormalize.isSelected();
		Main.deleteDir(new File(System.getProperty("user.home") + "/.firestar/temp/")); // starts with clean temp
		new Thread(() -> {
			int progressSize = tracklist.size()+(sptrack != null?1:0)+(mptrack != null?1:0)+1; // Accounting for processes

			progressDialog = new Scooter();
			progressDialog.showDialog("Soundtrack Mod Generator");
			progressDialog.setText("Generating audio files...");
			progressDialog.setProgressMax(progressSize);
			progressDialog.setProgressValue(0);
			progressDialog.progressBar.setStringPainted(false);

			try {
			new File(Main.inpath + "temp/data/audio/music").mkdirs();
			FileOutputStream fos = new FileOutputStream(new File(Main.inpath + "temp/fscript"));
			PrintStream ps = new PrintStream(fos);
			ps.println("fscript 1");
			ps.println("# AUTOGENERATED BY FIRESTAR");
			new File(Main.inpath + "temp/ffmpeg/").mkdirs();
			for (int i = 0; i < tracklist.size(); i++) {
				AudioTrack at = tracklist.get(i);
				String trackno = String.format("%02d", i+1);
				String oTitle;
				if (at.title == null) {oTitle = "";} else {oTitle = at.title;}
				String oArtist;
				if (at.artist == null) {oArtist = "";} else {oArtist = at.artist;}
				new File(Main.inpath + "temp/data/audio/music/" + trackno).mkdirs();
				if (at.path.getName().endsWith(".at9")) {
				progressDialog.setText("Copying track " + (i+1) + " out of " + tracklist.size() + "...");
				try {
					// Assume whoever made the AT9s knows what they're doing
					System.out.println("Copying track #" + (i+1) + " \"" + at.artist + " - " + at.title + "\"...");
					Files.copy(at.path.toPath(), Paths.get(Main.inpath + "temp/data/audio/music/" + trackno + "/music_stereo.at9"), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException ex) {
					Logger.getLogger(Suggs.class.getName()).log(Level.SEVERE, null, ex);
				}
				} else {
				progressDialog.setText("Encoding track " + (i+1) + " out of " + tracklist.size() + "...");
				try {
					System.out.println("Encoding track #" + (i+1) + " \"" + oArtist + " - " + oTitle + "\"...");
					if (!at.path.getName().endsWith(".wav") && !at.path.getName().endsWith(".wave")) { // convert to WAV first if it's not readable by at9tool yet
						Process p = Main.exec(new String[]{Main.inpath + "ffmpeg.exe", "-y", "-i", at.path.getPath(), "ffmpeg/" + at.path.getName() + ".wav"}, Main.inpath + "temp/");
						p.waitFor();
						at.path = new File(Main.inpath + "temp/ffmpeg/" + at.path.getName() + ".wav");
					}
					if (normalizeVolumes) { // normalize tracks
						Process p = Main.exec(new String[]{Main.inpath + "ffmpeg.exe", "-y", "-i", at.path.getPath(), "-filter", "loudnorm=linear=true:i=-5.0:lra=7.0:tp=0.0", /*  force sample rate to prevent Random Stupid Bullshit™  */"-ar", "44100", "ffmpeg/" + at.path.getName() + "_normalized.wav"}, Main.inpath + "temp/");
						p.waitFor();
						at.path = new File(Main.inpath + "temp/ffmpeg/" + at.path.getName() + "_normalized.wav");
					}
					Process p = Main.exec(new String[]{Main.inpath + "at9tool.exe", "-e", "-br", "144", at.path.getPath(), "data/audio/music/" + trackno + "/music_stereo.at9"}, Main.inpath + "temp/");
					p.waitFor();
				} catch (IOException | InterruptedException ex) {
					Logger.getLogger(Suggs.class.getName()).log(Level.SEVERE, null, ex);
				}
				}
				String ocmd = "modify";
				if (i >= 12) ocmd = "create";
				String oArtistLine;
				if (!oArtist.isEmpty()) {
					oArtistLine = oArtist.replace("\"", "\\\"")+"\\n";
				} else {
					oArtistLine = "";
				}
				int i2 = 0;
				while (i2 < 15 /*17 languages*/) {
					String language = "INTERNAL_ERROR";
					switch (i2) {
						case 0 -> language = "american";
						case 1 -> language = "danish";
						case 2 -> language = "dutch";
						case 3 -> language = "english";
						case 4 -> language = "finnish";
						case 5 -> language = "french";
						case 6 -> language = "german";
						case 7 -> language = "italian";
						case 8 -> language = "japanese";
						case 9 -> language = "norwegian";
						case 10 -> language = "polish";
						case 11 -> language = "portuguese";
						case 12 -> language = "russian";
						case 13 -> language = "spanish";
						case 14 -> language = "swedish";
						default -> {
							int result = JOptionPane.showConfirmDialog(frame, "Firestar encountered an internal error.\nString 'language' exported to FSCRIPT was blank.", "Fatal Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
							if (result == JOptionPane.OK_OPTION) {System.exit(1);} //user safety
						}
					}
				//case 9: language = "korean"; break;
				//case 16: language = "traditionalchinese"; break;
					ps.println("file \"data/plugins/languages/"+language+"/entries.xml\" xml "+ocmd+" StringTable.entry#MT_"+trackno+" set attribute \"string\" \""+oArtistLine+oTitle.replace("\"", "\\\"")+"\"");
					i2++;
				}
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
					if (!sptrack.getName().endsWith(".wav") && !sptrack.getName().endsWith(".wave")) { // convert to WAV first if it's not readable by at9tool yet
						Process p = Main.exec(new String[]{Main.inpath + "ffmpeg.exe", "-y", "-i", sptrack.getPath(), "ffmpeg/" + sptrack.getName() + ".wav"}, Main.inpath + "temp/");
						p.waitFor();
						sptrack = new File(Main.inpath + "temp/ffmpeg/" + sptrack.getName() + ".wav");
					}
					if (normalizeVolumes) { // normalize tracks
						Process p = Main.exec(new String[]{Main.inpath + "ffmpeg.exe", "-y", "-i", sptrack.getPath(), "-filter", "loudnorm=linear=true:i=-10.0:lra=12.0:tp=-2.0", /*  force sample rate to prevent Random Stupid Bullshit™  */"-ar", "44100", "ffmpeg/" + sptrack.getName() + "_normalized.wav"}, Main.inpath + "temp/");
						p.waitFor();
						sptrack = new File(Main.inpath + "temp/ffmpeg/" + sptrack.getName() + "_normalized.wav");
					}
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
					if (!mptrack.getName().endsWith(".wav") && !mptrack.getName().endsWith(".wave")) { // convert to WAV first if it's not readable by at9tool yet
						Process p = Main.exec(new String[]{Main.inpath + "ffmpeg.exe", "-y", "-i", mptrack.getPath(), "ffmpeg/" + mptrack.getName() + ".wav"}, Main.inpath + "temp/");
						p.waitFor();
						mptrack = new File(Main.inpath + "temp/ffmpeg/" + mptrack.getName() + ".wav");
					}
					if (normalizeVolumes) { // normalize tracks
						Process p = Main.exec(new String[]{Main.inpath + "ffmpeg.exe", "-y", "-i", mptrack.getPath(), "-filter", "loudnorm=linear=true:i=-10.0:lra=12.0:tp=-2.0", /*  force sample rate to prevent Random Stupid Bullshit™  */"-ar", "44100", "ffmpeg/" + mptrack.getName() + "_normalized.wav"}, Main.inpath + "temp/");
						p.waitFor();
						mptrack = new File(Main.inpath + "temp/ffmpeg/" + mptrack.getName() + "_normalized.wav");
					}
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
			Main.deleteDir(new File(Main.inpath + "temp/ffmpeg/"));
			Clifford saveDialog = new Clifford();
			saveDialog.isSoundtrack = true;
			saveDialog.Action(frame, new File(Main.inpath + "temp/"));
			parent.setEnabled(true);
		}).start();
	}
}
