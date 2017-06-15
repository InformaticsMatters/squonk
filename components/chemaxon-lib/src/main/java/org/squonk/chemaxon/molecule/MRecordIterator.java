/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.chemaxon.molecule;

import chemaxon.formats.MFileFormatUtil;
import chemaxon.marvin.io.MRecord;
import chemaxon.marvin.io.MRecordParseException;
import chemaxon.marvin.io.MRecordReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public class MRecordIterator implements Iterator<MRecord>, Closeable {

    private static final Logger LOG = Logger.getLogger(MRecordIterator.class.getName());

    private MRecordReader recordReader;
    private MRecord nextRecord;
    private int count = 0;
    private final StackTraceElement[] initiatorStackTrace;

    public MRecordIterator(InputStream is)  {
        try {
            recordReader = MFileFormatUtil.createRecordReader(is, null, null, null);
        } catch (IOException ex) {
            Logger.getLogger(MRecordIterator.class.getName()).log(Level.SEVERE, null, ex);
        }
        initiatorStackTrace = Thread.currentThread().getStackTrace();
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
        if (nextRecord != null) {
            return true;
        }
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
        MRecord next = nextRecord;
        nextRecord = null;
        return next;
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
            close(recordReader);
            LOG.log(Level.WARNING, "******* MRecordIterator {0} not closed. Doing this in finalize() instead *******", toString());
            
            StringBuilder b = new StringBuilder();
            for (StackTraceElement el :initiatorStackTrace) {
                b.append("\t").append(el.toString()).append("\n");
            }
            LOG.log(Level.WARNING, "Call stack of constructor was:\n{0}", b.toString());
        }
    }

    private void close(MRecordReader reader) {
        if (reader != null) {
            try {
                reader.close();
                LOG.log(Level.FINE, "MRecordReader closed for {0}", toString());
            } catch (IOException ioe) {
                throw new RuntimeException("IOException closing MRecordReader", ioe);
            }
        }
        this.recordReader = null;
    }

    @Override
    public void close() throws IOException {
        LOG.log(Level.FINE, "Closing MRecordReader {0}", toString());
        //new Exception("StackTrace").printStackTrace();
        close(recordReader);
    }

}
