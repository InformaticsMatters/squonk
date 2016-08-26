package org.squonk.reader;

import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.BasicObject;
import org.squonk.util.IOUtils;
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
    private String type;
    private CSVParser parser;
    private Iterator<CSVRecord> iter;
    private Map<String, Integer> headermap;
    private final DatasetMetadata meta;
    private final Set<String> fields;
    private final String source = "CSV file"; // TODO try to get the file name passed through

    public CSVReader(InputStream is, String type) throws IOException {
        this(is, CSVFormat.DEFAULT, type);
    }

    public CSVReader(InputStream is, CSVFormat csvFormat, String type) throws IOException {
        this.reader = new InputStreamReader(IOUtils.getGunzippedInputStream(is));
        this.format = csvFormat;
        this.type = type;
        this.parser = format.parse(reader);
        this.iter = parser.iterator();
        this.headermap = parser.getHeaderMap();
        this.meta = new DatasetMetadata(BasicObject.class);
        this.fields = new HashSet<>();

        meta.getProperties().put(DatasetMetadata.PROP_CREATED, DatasetMetadata.now());
        meta.getProperties().put(DatasetMetadata.PROP_DESCRIPTION, "Created from CSV file of type " + type);
    }

    public DatasetMetadata getDatasetMetadata() {
        return meta;
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
            Map<String,String> values = rec.toMap();
            for (Map.Entry<String,String> e : values.entrySet()) {
                String name = e.getKey();
                bo.putValue(name, e.getValue());
                if (!fields.contains(name)) {
                    fields.add(name);
                    meta.createField(name, source, null, String.class);
                    meta.appendFieldHistory(name, "Value read from CSV file");
                }
            }
        } else {
            for (int i = 0; i < rec.size(); i++) {
                String name = "field" + (i + 1);
                bo.putValue(name, rec.get(i));
                if (!fields.contains(name)) {
                    fields.add(name);
                    meta.createField(name, source, null, String.class);
                    meta.appendFieldHistory(name, "Value read from CSV file");
                }
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
