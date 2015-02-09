package com.im.lac.chemaxon.molecule;

import chemaxon.formats.MFileFormatUtil;
import chemaxon.marvin.io.MRecord;
import chemaxon.marvin.io.MRecordParseException;
import chemaxon.marvin.io.MRecordReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public class MRecordIterator implements Iterator<MRecord>, Closeable {

    private static final Logger LOG = Logger.getLogger(MRecordIterator.class.getName());

    private final MRecordReader recordReader;
    private MRecord nextRecord;
    private int count = 0;

    public MRecordIterator(InputStream is) throws IOException {
        recordReader = MFileFormatUtil.createRecordReader(is, null, null, null);
    }

    /**
     * Public access in case direct access is needed during operation. Use with
     * care.
     *
     * @return The instance doing the parsing
     */
    public MRecordReader getRecordReader() {
        return recordReader;
    }

    @Override
    public boolean hasNext() {
        try {
            return read();
        } catch (IOException | MRecordParseException e) {
            throw new RuntimeException("Error reading record " + count, e);
        }
    }

    public boolean read() throws IOException, MRecordParseException {
        LOG.finer("Reading next ...");
        count++;
        MRecord rec = recordReader.nextRecord();

        if (rec != null) {
            nextRecord = rec;
            return true;
        } else {
            LOG.fine("Stream seems completed");
            nextRecord = null;
            //close(recordReader);
            return false;
        }
    }

    @Override
    public MRecord next() {
        if (nextRecord == null) {

            boolean success;
            try {
                success = read();
            } catch (IOException | MRecordParseException e) {
                throw new RuntimeException("Error reading record " + count, e);
            }

            if (!success) {
                //close(recordReader);
                throw new NoSuchElementException("No more records");
            }
        }
        return nextRecord;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove not supported");
    }

    @Override
    public void finalize() throws Throwable {
        super.finalize();
        // ensure always closed. Whole file may not be read.
        if (recordReader != null) {
            LOG.warning("******* Reader not closed. Doing this in finalize() instead *******");
            close(recordReader);
        }
    }

    private void close(MRecordReader reader) {
        if (reader != null) {
            try {
                LOG.info("Closing MRecordReader");
                //new Exception("Test only").printStackTrace();
                reader.close();
            } catch (IOException ioe) {
                throw new RuntimeException("IOException closing MRecordReader", ioe);
            }
        }
        reader = null;
    }

    @Override
    public void close() throws IOException {
        close(recordReader);
    }

}
