package com.squonk.reader;

import com.im.lac.types.MoleculeObject;
import com.im.lac.types.MoleculeObjectIterable;
import java.io.*;
import java.util.*;

/** Lightweight SDF reader that takes SDF input and generates an Iterator/Iterable of 
 * MoleculeObjects.
 * Does NOT attempt to check that the the generated molfiles are valid, so errors should
 * be expected downstream when parsing the individual molecules.
 * This class is not thread safe.
 * 
 * @author timbo
 */
public class SDFReader implements MoleculeObjectIterable, Iterator<MoleculeObject>, AutoCloseable {

    private LineNumberReader reader = null;
    private MoleculeObject molobj = null;

    public SDFReader(InputStream is) throws IOException {
        this.reader = new LineNumberReader(new InputStreamReader(is));
        this.molobj = readRow();
    }

    @Override
    public Iterator<MoleculeObject> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return this.molobj != null;
    }

    @Override
    public MoleculeObject next() {
        MoleculeObject row = this.molobj;
        if (row == null) {
            throw new NoSuchElementException();
        }
        this.molobj = readRow();
        return row;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    private MoleculeObject readRow() {
        try {
            MoleculeObject mo = readMolfile();
            readData(mo);
            return mo;
        } catch (EOFException eofe) {
            return null;
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private MoleculeObject readMolfile() throws IOException {

        LineNumberReader reader = ensureOpen();
        StringBuilder sb = new StringBuilder();
        // Header block
        String first = reader.readLine();
        String second = reader.readLine();
        String third = reader.readLine();

        if (first == null || second == null || third == null) {
            throw new EOFException();
        }

        sb.append(first).append(NEWLINE);
        sb.append(second).append(NEWLINE);
        sb.append(third).append(NEWLINE);

        String sep = "";

        // Ctab block
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                throw new EOFException();
            }
            sb.append(sep);
            sep = NEWLINE;
            sb.append(line);
            if (line.startsWith("M  END")) {
                break;
            }
        }

        String molfile = sb.toString();
        return new MoleculeObject(molfile, "mol");
    }

    private void readData(MoleculeObject mo) throws IOException {
        LineNumberReader reader = ensureOpen();

        fields:
        for (int i = 0; true; i++) {
            String line = reader.readLine();
            if (line == null) {
                throw new EOFException();
            } // End if

            if (line.equals("") && i == 0) {
                // Extra blank line between the end of the molfile and the beginning of the first data item
            } else if (line.startsWith(">")) {
                int nameBegin = line.indexOf('<');
                int nameEnd = line.indexOf('>', nameBegin);

                String name = line.substring(nameBegin + 1, nameEnd);
                StringBuilder sb = new StringBuilder();
                String sep = "";

                while (true) {
                    line = reader.readLine();
                    if (line == null) {
                        throw new EOFException();
                    }

                    if (line.equals("")) {
                        break;
                    } else if (line.equals("$$$$")) {
                        break fields;
                    }

                    sb.append(sep);
                    sep = NEWLINE;
                    sb.append(line);
                }
                mo.putValue(name, sb.toString());
            } else if (line.equals("$$$$")) {
                break fields;
            } else {
                throw new IOException("Error parsing at line " + String.valueOf(reader.getLineNumber() + 1));
            }
        }
    }

    private LineNumberReader ensureOpen() throws IOException {

        if (this.reader == null) {
            throw new IOException();
        }
        return this.reader;
    }

    public void close() throws IOException {

        try {
            if (this.reader != null) {
                this.reader.close();
            }
        } finally {
            this.reader = null;
        }
    }

    private static final String NEWLINE = "\n";
}
