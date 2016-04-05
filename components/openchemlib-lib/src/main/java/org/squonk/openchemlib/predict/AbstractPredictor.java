package org.squonk.openchemlib.predict;

import com.actelion.research.chem.StereoMolecule;
import com.im.lac.types.MoleculeObject;
import org.squonk.openchemlib.molecule.OCLMoleculeUtils;
import org.squonk.property.Predictor;
import org.squonk.property.Property;

/**
 * Created by timbo on 05/04/16.
 */
public abstract class AbstractPredictor<V,T> extends Predictor<V,T> {

    public AbstractPredictor(String resultName, Property<V,T> propertyType) {
        super(resultName, propertyType);
    }

    public V calculate(MoleculeObject mo) {
        return calculate(mo, false, false);
    }

    public V calculate(MoleculeObject mo, boolean storeMol, boolean storeResult) {
        StereoMolecule mol = OCLMoleculeUtils.fetchMolecule(mo, storeMol);
        V result = calculate(mol);
        if (storeResult) {
            mo.putValue(getResultName(), result);
        }
        return result;
    }

    public abstract V calculate(StereoMolecule mol);
}
