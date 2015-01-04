package com.im.lac.chemaxon.io;

import chemaxon.formats.MFileFormatUtil;
import chemaxon.formats.MolFormatException;
import chemaxon.formats.MolImporter;
import chemaxon.marvin.io.MPropHandler;
import chemaxon.marvin.io.MRecord;
import chemaxon.marvin.io.MRecordParseException;
import chemaxon.marvin.io.MRecordReader;
import chemaxon.struc.MProp;
import chemaxon.struc.Molecule;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.im.lac.chemaxon.molecule.MoleculeConstants;

/**
 * Created by timbo on 14/04/2014.
 */
public class MoleculeIOUtils implements MoleculeConstants {

    static Logger log = Logger.getLogger(MoleculeIOUtils.class.getName());

    /**
     * Creates an Iterator of MRecords from the InputStream Designed for
     * splitting a file or stream into individual records without generating a
     * molecule instance. Can be used as a Camel splitter.
     * <code>split().method(MoleculeIOUtils.class, "mrecordIterator")</code>
     *
     * @param is The input molecules in any format that Marvin recognises
     * @return Iterator or records
     * @throws IOException
     */
    public Iterator<MRecord> mrecordIterator(final InputStream is) throws IOException {

        log.log(Level.FINE, "Creating Iterator<MRecord> for %s", is.toString());

        final MRecordReader recordReader = MFileFormatUtil.createRecordReader(is, null, null, null);

        return new Iterator<MRecord>() {

            private MRecord nextRecord;
            int count = 0;

            /**
             * Public access in case direct access is needed during operation.
             * Use with care.
             *
             * @return The instance doing the parsing
             */
            public MRecordReader getRecordReader() {
                return recordReader;
            }

            public boolean hasNext() {
                try {
                    return read();
                } catch (Exception e) {
                    throw new RuntimeException("Error reading record " + count, e);
                }
            }

            public boolean read() throws IOException, MRecordParseException {
                log.finer("Reading next ...");
                count++;
                MRecord rec = recordReader.nextRecord();

                if (rec != null) {
                    nextRecord = rec;
                    return true;
                } else {
                    log.fine("Stream seems completed");
                    nextRecord = null;
                    close(recordReader);
                    return false;
                }
            }

            public MRecord next() {
                if (nextRecord == null) {

                    boolean success;
                    try {
                        success = read();
                    } catch (Exception e) {
                        throw new RuntimeException("Error reading record " + count, e);
                    }

                    if (!success) {
                        close(recordReader);
                        throw new NoSuchElementException("No more records");
                    }
                }
                return nextRecord;
            }

            public void remove() {
                throw new UnsupportedOperationException("Remove not supported");
            }

            @Override
            public void finalize() {
                // ensure always closed. Whole file may not be read.
                if (recordReader != null) {
                    log.info("Reader not closed. Doing this in finalize() instead.");
                    close(recordReader);
                }
            }

            private void close(MRecordReader reader) {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ioe) {
                        throw new RuntimeException("IOException closing MRecordReader", ioe);
                    }
                }
                reader = null;
            }

        };
    }

    /**
     * Creates an Iterator of Molecules from the InputStream Can be used as a
     * Camel splitter.
     * <code>split().method(MoleculeIOUtils.class, "moleculeIterator")</code>
     *
     * @param is The input molecules in any format that Marvin recognises
     * @return Iterator of Molecules
     * @throws IOException
     */
    public static Iterator<Molecule> moleculeIterator(final InputStream is) throws IOException {
        MolImporter importer = new MolImporter(is);
        return importer.iterator();
    }

    public static Map<String, String> mrecordToMap(MRecord record) {
        Map<String, String> vals = new HashMap<String, String>();
        vals.put(STRUCTURE_FIELD_NAME, record.getString());
        String[] fields = record.getPropertyContainer().getKeys();
        List<MProp> values = record.getPropertyContainer().getPropList();
        for (int x = 0; x < fields.length; x++) {
            vals.put(fields[x], MPropHandler.convertToString(values.get(x), null));
        }
        return vals;
    }

    public static Molecule convertToMolecule(String s) throws MolFormatException {
        return MolImporter.importMol(s);
    }

    public static Molecule convertToMolecule(byte[] b) throws MolFormatException {
        return MolImporter.importMol(b);
    }

    public static Molecule convertToMolecule(Blob b) throws MolFormatException, SQLException {
        byte[] bytes = b.getBytes(1, (int) b.length());
        return MolImporter.importMol(bytes);
    }

    public static Molecule convertToMolecule(Clob c) throws MolFormatException, SQLException {
        String s = c.getSubString(1, (int) c.length());
        return convertToMolecule(s);
    }
/** Takes a Map of properties, one of which is a Molecule in some form and returns
 * a Molecule (converted as necessary) with the additional values from the Map set 
 * as properties of the Molecule (see Molecule.getProperties()).
 * 
 * @param map The input
 * @param structureKey The key under which the structure is located
 * @return The Molecule
 * @throws MolFormatException
 * @throws SQLException 
 */
    public static Molecule convertToMolecule(Map<String, Object> map, String structureKey) throws MolFormatException, SQLException {

        Object v = map.get(structureKey);
        Molecule mol = null;
        if (v.getClass() == String.class) {
            mol = convertToMolecule((String) v);
        } else if (v.getClass() == byte[].class) {
            mol = convertToMolecule((byte[]) v);
        } else if (v.getClass() == Blob.class) {
            mol = convertToMolecule((Blob) v);
        } else if (v.getClass() == Clob.class) {
            mol = convertToMolecule((Clob) v);
        } else if (v.getClass() == Molecule.class) {
            mol = (Molecule)v;
        } else {
            throw new IllegalArgumentException("Unsupported conversion for Molecule: "
                    + v.getClass().getName());
        }

        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (!e.getKey().equals(structureKey)) {
                mol.properties().setObject(e.getKey(), e.getValue());
            }
        }

        return mol;
    }

}
