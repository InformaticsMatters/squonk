package org.squonk.types;

import org.squonk.util.CommonMimeTypes;

import java.io.InputStream;

/** Wrapper around data from a Tripos Mol2 file to allow strong typing and type conversion.
 * Not to be confused with MolFile which is for MDL Molfile format.
 *
 * @author timbo
 */
public class Mol2File extends AbstractStreamType {

    private static final String MEDIA_TYPE = CommonMimeTypes.MIME_TYPE_TRIPOS_MOL2;

    public Mol2File(InputStream input) {
        super(input);
    }

    public String getMediaType() {
        return MEDIA_TYPE;
    }
    
}
