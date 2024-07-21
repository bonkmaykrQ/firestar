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

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.github.difflib.patch.PatchFailedException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Rizzo {
    private Scanner scanner;
    private final int maxVer = 1;
    private int ver = 1;
    private String workingDir;
    
    public Rizzo(File infile, String workingDir) throws FileNotFoundException, FirescriptFormatException {
	if (!workingDir.endsWith("/")) this.workingDir = workingDir + "/";
	else this.workingDir = workingDir;
	scanner = new Scanner(infile);
	parseScript(null);
    }
    
    private void parseScript(Object context) throws FirescriptFormatException {
	while (scanner.hasNextLine()) {
	    String line = scanner.nextLine().trim();
	    if (line.startsWith("#") || line.length() < 1) continue;
	    if (!parseArgs(translateCommandline(line), context)) break;
	}
    }
    
    private boolean parseArgs(String[] args, Object context) throws FirescriptFormatException {
	if (args.length > 0) {
	    if (context == null) {
		System.out.println("Parsing Command: " + Arrays.toString(args));
		if (args[0].equalsIgnoreCase("fscript")) {
		    ver = Integer.parseInt(args[1]); // We'll do shit with this later
		    if (ver > maxVer) throw new FirescriptFormatException(args[0], "script too new");
		} else if (args[0].equalsIgnoreCase("file")) {
		    File newCtx = new File(workingDir + args[1]);
		    System.out.println("Calling new parse: " + Arrays.toString(Arrays.copyOfRange(args, 2, args.length)));
		    parseArgs(Arrays.copyOfRange(args, 2, args.length), newCtx);
		} else throw new FirescriptFormatException("fscript", "unknown command '" + args[0] + "'");
	    } else {
		System.out.println("Parsing Command: " + Arrays.toString(args) + " with context: " + context.getClass().getName() + "@" + context.hashCode());
		if (args[0].equals("{")) {
		    System.out.println("New context parse: " + context.getClass().getName() + "@" + context.hashCode());
		    parseScript(context);
		} else if (args[0].equals("}")) {
		    System.out.println("Ending context block: " + context.getClass().getName() + "@" + context.hashCode());
		    return false;
		} else if (context instanceof File file) {
		    if (!file.exists()) return true;
		    if (args[0].equalsIgnoreCase("delete")) {
			System.out.println("Deleting: " + file.getPath());
			if (file.getAbsolutePath().startsWith(workingDir)) {
			    if (file.isDirectory()) Main.deleteDir(file);
			    else file.delete();
			}
		    } else if (args[0].equalsIgnoreCase("xml")) {
			try {
			    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			    // Mmmm, love me some INVALID XML corrections
			    ReplacingInputStream ris = new ReplacingInputStream(new ReplacingInputStream(new FileInputStream(file), "&", "&amp;"), "\n", "&#10;");
			    Document doc = docBuilder.parse(ris);
			    Node n = doc.createProcessingInstruction(StreamResult.PI_DISABLE_OUTPUT_ESCAPING, "\n");
			    parseArgs(Arrays.copyOfRange(args, 1, args.length), doc);
			    try {
				FileOutputStream output = new FileOutputStream(file);
				Transformer transformer = TransformerFactory.newInstance().newTransformer();

				StreamResult result = new StreamResult(new StringWriter());
				DOMSource source = new DOMSource(doc);

				transformer.transform(source, result);
				// Look ma, I'm breaking XML standards!
				String xmlString = result.getWriter().toString()
					.replace("&amp;", "&")
					.replace("&#9;", "\t")
					.replace("&#8;", "\b")
					.replace("&#10;", "\n")
					.replace("&#13;", "\r")
					.replace("&#12;", "\f");
				try (PrintStream ps = new PrintStream(output)) {
				    ps.print(xmlString);
				}
			    } catch (TransformerException ex) {
				Logger.getLogger(Rizzo.class.getName()).log(Level.SEVERE, null, ex);
			    }
			} catch (SAXException | IOException | ParserConfigurationException ex) {
			    Logger.getLogger(Rizzo.class.getName()).log(Level.SEVERE, null, ex);
			}
		    } else if (args[0].equalsIgnoreCase("str") || args[0].equalsIgnoreCase("xstr")) {
			try {
			    FileInputStream fis = new FileInputStream(file);
			    ByteArrayInputStream bais = new ByteArrayInputStream(fis.readAllBytes());
			    fis.close();
			    FileOutputStream fos = new FileOutputStream(file);
			    if (args[1].equalsIgnoreCase("append")) {
				bais.transferTo(fos);
				for (int s = 0; s < args[2].length(); s++) {
				    char c = args[2].charAt(s);
				    fos.write(c);
				}
			    } else if (args[1].equalsIgnoreCase("replace") || args[1].equalsIgnoreCase("delete")) {
				String replacement = "";
				if (args[1].equalsIgnoreCase("replace")) replacement = args[3];
				ReplacingInputStream ris;
				if (args[0].equalsIgnoreCase("xstr")) ris = new ReplacingInputStream(bais, args[2], replacement, false);
				else ris = new ReplacingInputStream(bais, args[2], replacement);
				ris.transferTo(fos);
				ris.close();
			    }
			    fos.flush();
			    fos.close();
			} catch (IOException ex) {
			    Logger.getLogger(Rizzo.class.getName()).log(Level.SEVERE, null, ex);
			}
		    } else if (args[0].equalsIgnoreCase("binedit")) {
			int offset = Integer.parseInt(args[1]);
			String bytes = args[2];
			if (bytes.length() % 2 != 0) throw new FirescriptFormatException(args[0], "invalid length of bytes");
			try {
			    byte[] ba;
			    try (FileInputStream fis = new FileInputStream(file)) {
				ba = fis.readAllBytes();
			    }
			    if (offset >= ba.length) throw new FirescriptFormatException(args[0], "offset is larger than file size");
			    else {
				byte[] in = HexFormat.of().parseHex(bytes);
				System.arraycopy(in, 0, ba, offset, in.length);
				try (FileOutputStream fos = new FileOutputStream(file)) {
				    fos.write(ba);
				    fos.flush();
				}
			    }
			} catch (IOException ex) {
			    Logger.getLogger(Rizzo.class.getName()).log(Level.SEVERE, null, ex);
			}
		    } else if (args[0].equalsIgnoreCase("patch")) {
			try {
			    List<String> original = Files.readAllLines(file.toPath());
			    File patchFile = new File(workingDir + args[1]);
			    if (!patchFile.exists()) throw new FirescriptFormatException(args[0], "patch file doesn't exist");
			    List<String> patched = Files.readAllLines(patchFile.toPath());
				    
			    Patch<String> patch = UnifiedDiffUtils.parseUnifiedDiff(patched);
			    List<String> result = DiffUtils.patch(original, patch);
			    
			    try (FileWriter fileWriter = new FileWriter(file)) {
				for (String str : result) {
				    fileWriter.write(str + "\r\n");
				}
			    }
			} catch (FirescriptFormatException | PatchFailedException | IOException ex) {
			    Logger.getLogger(Rizzo.class.getName()).log(Level.SEVERE, null, ex);
			}
		    } else throw new FirescriptFormatException("file", "unknown command '" + args[0] + "'");
		} else if (context instanceof Document document) {
		    if (args[0].equalsIgnoreCase("modify")) {
			Element elem = (Element)traverse(document, args[1]);
			parseArgs(Arrays.copyOfRange(args, 2, args.length), elem);
		    } else if (args[0].equalsIgnoreCase("create")) {
			String newTag = args[1].substring(args[1].lastIndexOf(".")+1);
			String newID = "";
			if (newTag.contains("#")) {
			    newID = newTag.substring(newTag.indexOf("#")+1);
			    newTag = newTag.substring(0, newTag.indexOf("#"));
			}
			Element newElem = document.createElement(newTag);
			if (newID != null && newID.length() > 0) newElem.setAttribute("id", newID);
			traverse(document, args[1].substring(0, args[1].lastIndexOf("."))).appendChild(newElem);
			parseArgs(Arrays.copyOfRange(args, 2, args.length), newElem);
		    } else if (args[0].equalsIgnoreCase("delete")) {
			Element elem = (Element)traverse(document, args[1]);
			elem.getParentNode().removeChild(elem);
		    } else if (args[0].equalsIgnoreCase("merge")) {
			// We're basically copying the file context xml command but with another xml document.
			try {
			    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			    
			    ReplacingInputStream ris = new ReplacingInputStream(new FileInputStream(new File(workingDir + args[1])), "&", "&amp;");
			    Document outDoc = docBuilder.parse(ris);
			    
			    NamedNodeMap nnm = outDoc.getDocumentElement().getAttributes();
			    for (int x = 0; x < nnm.getLength(); x++) {
				Attr importedNode = (Attr) document.importNode(nnm.item(x), true);
				document.getDocumentElement().setAttributeNodeNS(importedNode);
			    }
			    
			    NodeList cn = outDoc.getDocumentElement().getChildNodes();
			    for (int x = 0; x < cn.getLength(); x++) {
				Node importedNode = document.importNode(cn.item(x), true);
				document.getDocumentElement().appendChild(importedNode);
			    }
			} catch (SAXException | IOException | ParserConfigurationException ex) {
			    Logger.getLogger(Rizzo.class.getName()).log(Level.SEVERE, null, ex);
			}
		    } else throw new FirescriptFormatException("xml", "unknown command '" + args[0] + "'");
		} else if (context instanceof Element element) {
		    if (args[0].equalsIgnoreCase("set")) {
			if (args[1].equalsIgnoreCase("attribute"))
			    // We replace newlines on XML write.
			    element.setAttribute(args[2], args[3].replace("\\n", "&#10;"));
			else if (args[1].equalsIgnoreCase("value"))
			    element.setNodeValue(args[2]);
		    } else if (args[0].equalsIgnoreCase("create")) {
			String newTag = args[1].substring(args[1].lastIndexOf(".")+1);
			String newID = "";
			if (newTag.contains("#")) {
			    newID = newTag.substring(newTag.indexOf("#")+1);
			    newTag = newTag.substring(0, newTag.indexOf("#"));
			}
			Element newElem = element.getOwnerDocument().createElement(newTag);
			if (newID != null && newID.length() > 0) newElem.setAttribute("id", newID);
			traverse(element, args[1].substring(0, args[1].lastIndexOf("."))).appendChild(newElem);
			parseArgs(Arrays.copyOfRange(args, 2, args.length), newElem);
		    } else throw new FirescriptFormatException("");
		} else throw new FirescriptFormatException("context is unknown");
	    }
	}
	return true;
    }
    
    private Node traverse(Node owner, String selector) throws FirescriptFormatException {
	if (selector == null || selector.length() == 0 || owner == null) {
	    return null;
	}
	String[] elems = selector.split("\\.");
	Node parent = owner;
	for (String tag : elems) {
	    Node newParent = null;
	    int index = 0;
	    if (tag.contains("[")) {
		index = Integer.parseInt(tag.substring(tag.indexOf("[")+1, tag.lastIndexOf("]")));
		tag = tag.substring(0, tag.indexOf("["));
	    }
	    
	    String id = "";
	    if (tag.contains("#")) {
		id = tag.substring(tag.indexOf("#")+1);
		tag = tag.substring(0, tag.indexOf("#"));
	    }
	    if (id.length() > 0) {
		NodeList ns;
		if (parent instanceof Document document) 
		    ns = document.getElementsByTagName(tag);
		else
		    ns = ((Element)parent).getElementsByTagName(tag); 
		for (int i = 0; i < ns.getLength(); i++) {
		    Node n = ns.item(i);
		    if (((Element)n).getAttribute("id").equals(id)) {
			newParent = (Element)n;
			break;
		    }
		}
	    } else {
		if (parent instanceof Document document) 
		    newParent = document.getElementsByTagName(tag).item(index);
		else
		    newParent = ((Element)parent).getElementsByTagName(tag).item(index);
	    }
	    if (newParent == null) throw new FirescriptFormatException("xml: selector is invalid");
	    else parent = newParent;
	}
	return parent;
    }
    
    private static String[] translateCommandline(String line) throws FirescriptFormatException {
	if (line == null || line.length() == 0) {
	    return new String[0];
	}

	final int normal = 0;
	final int inQuote = 1;
	final int inDoubleQuote = 2;
	int state = normal;
	final ArrayList<String> result = new ArrayList<String>();
	final StringBuilder current = new StringBuilder();
	boolean lastTokenHasBeenQuoted = false;
	boolean lastTokenWasEscaped = false;

	for (int i = 0; i < line.length(); i++) {
	    char nextTok = line.charAt(i);
	    switch (state) {
		case inQuote -> {
		    if (nextTok == '\\') {
			lastTokenWasEscaped = true;
		    } else if (nextTok == '\'' && !lastTokenWasEscaped) {
			lastTokenHasBeenQuoted = true;
			state = normal;
		    } else {
			if (lastTokenWasEscaped) {
			    if (nextTok == 't') nextTok = '\t';
			    if (nextTok == 'b') nextTok = '\b';
			    if (nextTok == 'n') nextTok = '\n';
			    if (nextTok == 'r') nextTok = '\r';
			    if (nextTok == 'f') nextTok = '\f';
			}
			current.append(nextTok);
			lastTokenWasEscaped = false;
		    }
		}
		case inDoubleQuote -> {
		    if (nextTok == '\\') {
			lastTokenWasEscaped = true;
		    } else if (nextTok == '\"' && !lastTokenWasEscaped) {
			lastTokenHasBeenQuoted = true;
			state = normal;
		    } else {
			if (lastTokenWasEscaped) {
			    if (nextTok == 't') nextTok = '\t';
			    if (nextTok == 'b') nextTok = '\b';
			    if (nextTok == 'n') nextTok = '\n';
			    if (nextTok == 'r') nextTok = '\r';
			    if (nextTok == 'f') nextTok = '\f';
			}
			current.append(nextTok);
			lastTokenWasEscaped = false;
		    }
		}
		default -> {
		    switch (nextTok) {
			case '\\' -> lastTokenWasEscaped = true;
			case '\'' -> state = inQuote;
			case '\"' -> state = inDoubleQuote;
			case ' ' -> {
			    if (!lastTokenWasEscaped && (lastTokenHasBeenQuoted || current.length() != 0)) {
				result.add(current.toString());
				current.setLength(0);
			    }
			}
			default -> {
			    if (lastTokenWasEscaped) {
				if (nextTok == 't') nextTok = '\t';
				if (nextTok == 'b') nextTok = '\b';
				if (nextTok == 'n') nextTok = '\n';
				if (nextTok == 'r') nextTok = '\r';
				if (nextTok == 'f') nextTok = '\f';
				lastTokenWasEscaped = false;
			    }
			    current.append(nextTok);
			}
		    }
		    lastTokenHasBeenQuoted = false;
		}
	    }
	}
	if (lastTokenHasBeenQuoted || current.length() != 0) {
	    result.add(current.toString());
	}
	if (state == inQuote || state == inDoubleQuote) {
	    throw new FirescriptFormatException("unbalanced quotes in " + line);
	}
	return result.toArray(new String[result.size()]);
    }
}