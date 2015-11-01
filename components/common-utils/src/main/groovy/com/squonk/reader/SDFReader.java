package com.squonk.reader;

import com.im.lac.types.MoleculeObject;
import com.im.lac.types.MoleculeObjectIterable;
import com.squonk.util.IOUtils;
import java.io.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Lightweight SDF reader that takes SDF input and generates an
 * Iterator/Iterable of MoleculeObjects. Does NOT attempt to check that the the
 * generated molfiles are valid, so errors should be expected downstream when
 * parsing the individual molecules. This class is not thread safe.
 *
 * The low level file parsing logic is borrowed from here:
 * https://github.com/qsardb/qsardb-common/blob/master/conversion/sdfile/src/main/java/org/qsardb/conversion/sdfile/CompoundIterator.java
 *
 * @author timbo
 */
public class SDFReader implements MoleculeObjectIterable, Iterator<MoleculeObject>, AutoCloseable {

    private LineNumberReader reader;
    private MoleculeObject molobj;
    private boolean started = false;
    private String nameFieldName = "name";

    public SDFReader(InputStream is) throws IOException {
        this.reader = new LineNumberReader(new InputStreamReader(is));
    }

    /**
     * Get the value of nameFieldName
     *
     * @return the value of nameFieldName
     */
    public String getNameFieldName() {
        return nameFieldName;
    }

    /**
     * Set the field name that will be used for the molecule name (the
     * value of the first line in the CTAB block). This will become a value with
     * the specified name. Default is "name". Set to null if your don't want the
     * name to be added as a value.
     *
     * @param nameFieldName new value of nameFieldName
     */
    public void setNameFieldName(String nameFieldName) {
        this.nameFieldName = nameFieldName;
    }

    @Override
    public Iterator<MoleculeObject> iterator() {
        return this;
    }
    
    /**
     * Get the contents as a Stream
     * @return 
     */
    public Stream<MoleculeObject> asStream() {
        return IOUtils.streamFromIterator(this, MoleculeObject.class);
    }
    
    /**
     * Get the contents as a Stream of the specified batch size
     * @param batchSize
     * @return 
     */
    public Stream<MoleculeObject> asStream(int batchSize) {
        return IOUtils.streamFromIterator(this, MoleculeObject.class, batchSize);
    }

    @Override
    public boolean hasNext() {
        return this.molobj != null || !started;
    }

    @Override
    public MoleculeObject next() {
        if (!started) {
            started = true;
            this.molobj = readRow();
        }
        
        MoleculeObject row = this.molobj;
        if (row == null) {
            throw new NoSuchElementException();
        }
        // read the next one
        this.molobj = readRow();
        // return the current one
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

        LineNumberReader rdr = ensureOpen();
        StringBuilder sb = new StringBuilder();
        // Header block
        String first = rdr.readLine();
        String second = rdr.readLine();
        String third = rdr.readLine();

        if (first == null || second == null || third == null) {
            throw new EOFException();
        }

        sb.append(first).append(NEWLINE);
        sb.append(second).append(NEWLINE);
        sb.append(third).append(NEWLINE);

        String sep = "";

        // Ctab block
        while (true) {
            String line = rdr.readLine();
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
        MoleculeObject mo = new MoleculeObject(molfile, "mol");
        if (nameFieldName != null && !first.trim().isEmpty()) {
            mo.putValue(nameFieldName, first);
        }
        return mo;
    }

    private void readData(MoleculeObject mo) throws IOException {
        LineNumberReader rdr = ensureOpen();

        fields:
        for (int i = 0; true; i++) {
            String line = rdr.readLine();
            if (line == null) {
                throw new EOFException();
            }

            if (line.equals("") && i == 0) {
                // Extra blank line between the end of the molfile and the beginning of the first data item
            } else if (line.startsWith(">")) {
                int nameBegin = line.indexOf('<');
                int nameEnd = line.indexOf('>', nameBegin);

                String name = line.substring(nameBegin + 1, nameEnd);
                StringBuilder sb = new StringBuilder();
                String sep = "";

                while (true) {
                    line = rdr.readLine();
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
                throw new IOException("Error parsing at line " + String.valueOf(rdr.getLineNumber() + 1));
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
