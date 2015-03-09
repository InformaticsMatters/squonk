package com.im.lac.camel.chemaxon.converters;

import com.im.lac.util.OutputGenerator;
import com.im.lac.chemaxon.molecule.MoleculeObjectUtils;
import com.im.lac.chemaxon.molecule.MoleculeObjectWriter;
import com.im.lac.types.MoleculeObject;
import com.im.lac.types.MoleculeObjectIterable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.apache.camel.Converter;
import org.apache.camel.Exchange;

/**
 * Created by timbo on 21/04/2014.
 */
@Converter
public class MoleculeObjectConvertor {

    @Converter
    public static MoleculeObject createMoleculeObjectFromString(String mol, Exchange exchange) {
        return new MoleculeObject(mol);
    }

    /**
     * Create an Iterable of MoleculeObjects from an InputStream. IMPORTANT:
     * whilst the implementation type is not defined here it is certain that the
     * underlying stream will need to be closed. To do this check whether the
     * MoleculeObjectIterable that is returned implements the Closeable
     * interface and if so call the close method once you have finished
     * iterating. Ideally we should have an implementation that is independent
     * of Marvin as other chemistry implementations will need to depend on
     * ChemAxon even if this is the only thing they need.
     *
     * @param is
     * @param exchange
     * @return
     * @throws IOException
     */
    @Converter
    public static MoleculeObjectIterable createMoleculeObjectIterable(InputStream is, Exchange exchange)
            throws IOException {
        return MoleculeObjectUtils.createIterable(is);
    }

    /**
     * Create an Iterable of MoleculeObjects from an File. See the InputStream
     * variant for more details.
     *
     * @param file
     * @param exchange
     * @return
     * @throws java.io.IOException
     */
    @Converter
    public static MoleculeObjectIterable createMoleculeObjectIterable(File file, Exchange exchange)
            throws IOException {
        return MoleculeObjectUtils.createIterable(file);
    }

    /**
     *
     * @param moi
     * @return
     */
    @Converter
    public static OutputGenerator createOutputGeneratorFromMoleculeObjectIterable(MoleculeObjectIterable moi) {
        return new MoleculeObjectWriter(moi);
    }

}
