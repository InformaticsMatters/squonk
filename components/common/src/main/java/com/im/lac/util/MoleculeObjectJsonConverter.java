package com.im.lac.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.im.lac.types.MoleculeObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * DataFormat that handles marshaling/unmarshaling of MoleculeObjects to/from
 * JSON.
 *
 * @author timbo
 */
public class MoleculeObjectJsonConverter {

    private static final Logger LOG = Logger.getLogger(MoleculeObjectJsonConverter.class.getName());

    private static final String MIXED_FORMATS = "mixedFormats";

    /**
     * Should the input and output when marshaling be closed when processing is
     * complete. Default is true
     */
    private boolean autoCloseAfterMarshal = true;

    public MoleculeObjectJsonConverter() {
        this(true);
    }

    public MoleculeObjectJsonConverter(boolean autoCloseAfterMarshal) {
        this.autoCloseAfterMarshal = autoCloseAfterMarshal;
    }

    /**
     * Takes an Stream of MoleculeObjects and writes it to the OutputStream. The
     * Input and OutputStream are closed once processing is complete if the
     * autoCloseAfterMarshal field is set to true, otherwise both must be closed
     * by the caller.
     *
     * @param mols
     * @param outputStream
     * @return Metadata about the molecules and their properties
     * @throws IOException
     */
    public Metadata marshal(Stream<MoleculeObject> mols, OutputStream outputStream) throws IOException {

        Metadata meta = new Metadata();
        JsonFactory factory = new MappingJsonFactory();
        JsonGenerator generator = factory.createGenerator(outputStream);
        generator.writeStartArray();
        mols.forEachOrdered(mo -> {
            try {
                handleMetadata(meta, mo);
                generator.writeObject(mo);
            } catch (IOException ex) {
                throw new RuntimeException("Error generarting JSON", ex);
            }

        });

        generator.writeEndArray();
        generator.flush();

        if (autoCloseAfterMarshal) {
            IOUtils.closeIfCloseable(outputStream);
            IOUtils.closeIfCloseable(mols);
        }
        cleanMetadata(meta);
        return meta;
    }

    void cleanMetadata(Metadata meta) {
        if (MIXED_FORMATS.equals(meta.getMetadata(Metadata.FORMAT))) {
            meta.metaProps.remove(Metadata.FORMAT);
        }
    }

    void handleMetadata(Metadata meta, MoleculeObject mo) {
        meta.size++;
        String molFormat = mo.getFormat();
        String metaFormat = meta.getMetadata(Metadata.FORMAT, String.class);
        if (!MIXED_FORMATS.equals(metaFormat)) {
            if (metaFormat == null) {
                meta.metaProps.put(Metadata.FORMAT, molFormat);
            } else {
                if (molFormat != null && !metaFormat.equals(molFormat)) {
                    LOG.warning("Mixed formats encountered");
                    meta.metaProps.put(Metadata.FORMAT, MIXED_FORMATS);
                }
            }
        }
        for (Map.Entry<String, Object> e : mo.getValues().entrySet()) {
            String key = e.getKey();
            Object value = e.getValue();
            if (value != null) {
                Class type = value.getClass();
                Class storedType = meta.propertyTypes.get(key);
                if (storedType == null) {
                    meta.propertyTypes.put(key, type);
                } else if (storedType != type) {
                    throw new IllegalStateException("Can't store as JSON - value types must be homogeneous. Previous was " + storedType.getName() + ", encountered " + type.getName());
                }
            }
        }
    }

    /**
     * Generate an Stream of MoleculeObjects from the JSON input. NOTE: to
     * ensure the InputStream is closed you should either close the returned
     * stream or close the InputStream once processing is finished.
     *
     * @param in
     * @return A Stream of MoleculeObjects
     * @throws IOException
     */
    public Stream<MoleculeObject> unmarshal(InputStream in) throws IOException {
        MoleculeObjectUnmarshaler unmarshaller = new MoleculeObjectUnmarshaler(null);
        return unmarshaller.read(in);
    }
    
    public Stream<MoleculeObject> unmarshal(Metadata meta, InputStream in) throws IOException {
        MoleculeObjectUnmarshaler unmarshaller = new MoleculeObjectUnmarshaler(meta);
        return unmarshaller.read(in);
    }

}
