package org.squonk.types;

import org.squonk.util.CommonMimeTypes;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/** Wrapper around data from a MDL molfile to allow strong typing and type conversion
 *  Not to be confused with Mol2File which is for Tripos Mol2 format.
 *
 * @author timbo
 */
public class MolFile extends AbstractStreamType {

    private static final String MEDIA_TYPE = CommonMimeTypes.MIME_TYPE_MDL_MOLFILE;

    public MolFile(InputStream input) {
        super(input);
    }

    public MolFile(String input) {
        super(new ByteArrayInputStream(input.getBytes()));
    }

    public String getMediaType() {
        return MEDIA_TYPE;
    }
    
}
