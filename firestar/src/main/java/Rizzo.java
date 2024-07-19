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

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Rizzo {
    
    public void Rizzo(File infile) {
	
    }
    
    public static String[] translateCommandline(String toProcess) {
	if (toProcess == null || toProcess.length() == 0) {
	    return new String[0];
	}

	final int normal = 0;
	final int inQuote = 1;
	final int inDoubleQuote = 2;
	int state = normal;
	final StringTokenizer tok = new StringTokenizer(toProcess, "\"\' ", true);
	final ArrayList<String> result = new ArrayList<String>();
	final StringBuilder current = new StringBuilder();
	boolean lastTokenHasBeenQuoted = false;

	while (tok.hasMoreTokens()) {
	    String nextTok = tok.nextToken();
	    switch (state) {
		case inQuote:
		    if ("\'".equals(nextTok)) {
			lastTokenHasBeenQuoted = true;
			state = normal;
		    } else {
			current.append(nextTok);
		    }
		    break;
		case inDoubleQuote:
		    if ("\"".equals(nextTok)) {
			lastTokenHasBeenQuoted = true;
			state = normal;
		    } else {
			current.append(nextTok);
		    }
		    break;
		default:
		    if ("\'".equals(nextTok)) {
			state = inQuote;
		    } else if ("\"".equals(nextTok)) {
			state = inDoubleQuote;
		    } else if (" ".equals(nextTok)) {
			if (lastTokenHasBeenQuoted || current.length() != 0) {
			    result.add(current.toString());
			    current.setLength(0);
			}
		    } else {
			current.append(nextTok);
		    }
		    lastTokenHasBeenQuoted = false;
		    break;
	    }
	}
	if (lastTokenHasBeenQuoted || current.length() != 0) {
	    result.add(current.toString());
	}
	if (state == inQuote || state == inDoubleQuote) {
	    throw new RuntimeException("unbalanced quotes in " + toProcess);
	}
	return result.toArray(new String[result.size()]);
    }
}
