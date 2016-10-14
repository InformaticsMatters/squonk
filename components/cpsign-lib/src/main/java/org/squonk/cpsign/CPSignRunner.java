package org.squonk.cpsign;

import com.genettasoft.modeling.CPSignFactory;
import org.squonk.types.MoleculeObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.stream.Stream;

/**
 * Created by timbo on 14/10/2016.
 */
public class CPSignRunner {

    private CPSignFactory factory;

    public CPSignRunner() throws IOException {
        intialise();
    }


    public void intialise() throws IOException {
        // Start with instantiating CPSignFactory with your license
        factory = new CPSignFactory(new FileInputStream("/Users/timbo/dev/git/lac/data/licenses/cpsign0.3pro.license"));
    }

    public Stream<MoleculeObject> predictTCP(Stream<MoleculeObject> mols) {
               return null;
    }

}
