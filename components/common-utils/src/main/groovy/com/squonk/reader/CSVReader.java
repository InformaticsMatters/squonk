package com.squonk.reader;

import com.im.lac.types.BasicObject;
import com.squonk.util.IOUtils;
import java.io.*;
import java.util.*;
import java.util.stream.Stream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * CSV file reader
 *
 * @author timbo
 */
public class CSVReader implements Iterator<BasicObject>, AutoCloseable {

    private Reader reader;
    private CSVFormat format;
    private CSVParser parser;
    private Iterator<CSVRecord> iter;
    private Map<String, Integer> headermap;

    public CSVReader(InputStream is) throws IOException {
        this(is, CSVFormat.DEFAULT);
    }

    public CSVReader(InputStream is, CSVFormat csvFormat) throws IOException {
        this.reader = new InputStreamReader(IOUtils.getGunzippedInputStream(is));
        this.format = csvFormat;
        this.parser = format.parse(reader);
        this.iter = parser.iterator();
        this.headermap = parser.getHeaderMap();
    }

    public Iterator<BasicObject> iterator() {
        return this;
    }

    /**
     * Get the contents as a Stream
     *
     * @return
     */
    public Stream<BasicObject> asStream() {
        return IOUtils.streamFromIterator(this, BasicObject.class);
    }

    /**
     * Get the contents as a Stream of the specified batch size
     *
     * @param batchSize
     * @return
     */
    public Stream<BasicObject> asStream(int batchSize) {
        return IOUtils.streamFromIterator(this, BasicObject.class, batchSize);
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public BasicObject next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        CSVRecord rec = iter.next();
        return createBasicObject(rec);
    }

    protected BasicObject createBasicObject(CSVRecord rec) {
        BasicObject bo = new BasicObject();
        if (headermap != null) {
            Map values = rec.toMap();
            //System.out.println("VAL: " + values);
            bo.putValues(values);
        } else {
            for (int i = 0; i < rec.size(); i++) {
                bo.putValue("field" + (i + 1), rec.get(i));
            }
        }
        return bo;
    }

    @Override
    public void close() throws IOException {

        try {
            if (this.parser != null) {
                this.parser.close();
            }
        } finally {
            this.parser = null;
        }
    }

}
