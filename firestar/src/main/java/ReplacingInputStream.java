import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by simon on 8/29/17.
 * Copyright 2017 Simon Haoran Liang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
public class ReplacingInputStream extends FilterInputStream {

    private Queue<Integer> inQueue, outQueue;
    private final byte[] search, replacement;
    private boolean caseSensitive = true;
    
    public ReplacingInputStream(InputStream in, String search, String replacement) {
        super(in);

        this.inQueue = new LinkedList<>();
        this.outQueue = new LinkedList<>();

        this.search = search.getBytes();
        this.replacement = replacement.getBytes();
    }

    public ReplacingInputStream(InputStream in, String search, String replacement, boolean caseSensitive) {
        super(in);

        this.inQueue = new LinkedList<>();
        this.outQueue = new LinkedList<>();

        this.search = search.getBytes();
        this.replacement = replacement.getBytes();
	this.caseSensitive = caseSensitive;
    }

    private boolean isMatchFound() {
        Iterator<Integer> iterator = inQueue.iterator();

        for (byte b : search) {
	    if (!iterator.hasNext()) return false;
	    Integer i = iterator.next();
	    if (caseSensitive && b != i) return false;
	    else if (Character.toLowerCase(b) != Character.toLowerCase(i.byteValue())) return false;
        }

        return true;
    }

    private void readAhead() throws IOException {
        // Work up some look-ahead.
        while (inQueue.size() < search.length) {
            int next = super.read();
            inQueue.offer(next);

            if (next == -1) {
                break;
            }
        }
    }

    @Override
    public int read() throws IOException {
        // Next byte already determined.

        while (outQueue.isEmpty()) {
            readAhead();

            if (isMatchFound()) {
                for (byte a : search) {
                    inQueue.remove();
                }

                for (byte b : replacement) {
                    outQueue.offer((int) b);
                }
            } else {
                outQueue.add(inQueue.remove());
            }
        }

        return outQueue.remove();
    }

    @Override
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    // copied straight from InputStream inplementation, just needed to to use `read()` from this class
    @Override
    public int read(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int c = read();
        if (c == -1) {
            return -1;
        }
        b[off] = (byte)c;

        int i = 1;
        try {
            for (; i < len ; i++) {
                c = read();
                if (c == -1) {
                    break;
                }
                b[off + i] = (byte)c;
            }
        } catch (IOException ee) {
        }
        return i;
    }
}