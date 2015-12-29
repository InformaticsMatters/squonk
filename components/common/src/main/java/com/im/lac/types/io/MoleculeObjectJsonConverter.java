package com.im.lac.types.io;

import org.squonk.types.io.JsonHandler;
import com.im.lac.dataset.Metadata;
import com.im.lac.types.MoleculeObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Handlesmetadata driven marshaling/unmarshaling of MoleculeObjects to/from JSON.
 * The metadata among other things defines the Java classes of the MoleculeObject's 
 * values so that those can be handled as the correct Java types.
 * 
 * @deprecated This class is effectively obsoleted and should be replace by direct use of JsonHandler
 * @author timbo
 */
public class MoleculeObjectJsonConverter {

    private static final Logger LOG = Logger.getLogger(MoleculeObjectJsonConverter.class.getName());

    private static final String MIXED_FORMATS = "mixedFormats";
    
    private JsonHandler jsonHandler;

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
        this.jsonHandler = new JsonHandler();
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
         Metadata meta = new Metadata(MoleculeObject.class.getName(), Metadata.Type.ARRAY, 0);
         jsonHandler.marshalItems(mols, meta, outputStream);
         return meta;
     }
    

    /**
     * Generate an Stream of MoleculeObjects from the JSON input. 
     * NOTE: to ensure the InputStream is closed you should either close the 
     * returned stream or close the InputStream once processing is finished.
     *
     * @param meta
     * @param in
     * @return A Stream of MoleculeObjects
     * @throws IOException
     */
    public Stream<MoleculeObject> unmarshal(Metadata meta, InputStream in) throws Exception {
        return (Stream<MoleculeObject>)jsonHandler.unmarshalItemsAsStream(meta, in);
    }

}
