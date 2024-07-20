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

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Rizzo {
    private Scanner scanner;
    
    public Rizzo(File infile) throws FileNotFoundException, FirescriptFormatException {
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
		if (args[0].equalsIgnoreCase("file")) {
		    File newCtx = new File(Main.inpath + "temp/" + args[1]);
		    System.out.println("Calling new parse: " + Arrays.toString(Arrays.copyOfRange(args, 2, args.length)));
		    parseArgs(Arrays.copyOfRange(args, 2, args.length), newCtx);
		}
	    } else {
		System.out.println("Parsing Command: " + Arrays.toString(args) + " with context: " + context.getClass().getName() + "@" + context.hashCode());
		if (args[0].equals("{")) {
		    System.out.println("New context parse: " + context.getClass().getName() + "@" + context.hashCode());
		    parseScript(context);
		} else if (args[0].equals("}")) {
		    System.out.println("Ending context block: " + context.getClass().getName() + "@" + context.hashCode());
		    return false;
		} else if (context instanceof File file) {
		    if (args[0].equalsIgnoreCase("delete")) {
			System.out.println("Deleting: " + file.getPath());
			if (file.getAbsolutePath().startsWith(Main.inpath + "temp/"))
			    file.delete();
		    } else if (args[0].equalsIgnoreCase("xml")) {
			try {
			    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			    // Mmmm, love me some INVALID XML corrections
			    ReplacingInputStream ris = new ReplacingInputStream(new FileInputStream(file), "&", "&amp;");
			    Document doc = docBuilder.parse(ris);
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
				PrintStream ps = new PrintStream(output);
				ps.print(xmlString);
				ps.close();
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
		    }
		} else if (context instanceof Document document) {
		    if (args[0].equalsIgnoreCase("modify")) {
			Element elem = traverse(document, args[1]);
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
			Element elem = traverse(document, args[1]);
			elem.getParentNode().removeChild(elem);
		    }
		} else if (context instanceof Element element) {
		    if (args[0].equalsIgnoreCase("set")) {
			if (args[1].equalsIgnoreCase("attribute"))
			    element.setAttribute(args[2], args[3]);
			else if (args[1].equalsIgnoreCase("value"))
			    element.setNodeValue(args[2]);
		    }
		}
	    }
	}
	return true;
    }
    
    private Element traverse(Document doc, String selector) throws FirescriptFormatException {
	if (selector == null || selector.length() == 0 || doc == null) {
	    return null;
	}
	String[] elems = selector.split("\\.");
	Element parent = null;
	for (String tag : elems) {
	    Element newParent = null;
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
		if (parent != null) ns = parent.getElementsByTagName(tag);
		else ns = doc.getElementsByTagName(tag);
		for (int i = 0; i < ns.getLength(); i++) {
		    Node n = ns.item(i);
		    if (((Element)n).getAttribute("id").equals(id)) {
			newParent = (Element)n;
			break;
		    }
		}
	    } else {
		if (parent != null) newParent = (Element)parent.getElementsByTagName(tag).item(index);
		else newParent = (Element)doc.getElementsByTagName(tag).item(index);
	    }
	    if (newParent == null) throw new FirescriptFormatException();
	    else parent = newParent;
	}
	return parent;
    }
    
    private static String[] translateCommandline(String line) {
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
	    throw new RuntimeException("unbalanced quotes in " + line);
	}
	return result.toArray(new String[result.size()]);
    }
}