package org.squonk.openchemlib.predict;

import com.actelion.research.chem.StereoMolecule;
import org.squonk.types.MoleculeObject;
import org.squonk.openchemlib.molecule.OCLMoleculeUtils;
import org.squonk.property.MoleculeCalculator;
import org.squonk.property.Predictor;
import org.squonk.property.Property;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by timbo on 05/04/16.
 */
public abstract class AbstractOCLPredictor<V> extends Predictor<V,MoleculeObject,MoleculeCalculator<V>> {

    private static final Logger LOG = Logger.getLogger(AbstractOCLPredictor.class.getName());

    public AbstractOCLPredictor(String resultName, Property<V,MoleculeObject> propertyType) {
        super(resultName, propertyType);
    }


    public abstract class OCLCalculator<V> extends MoleculeCalculator<V> {

        public OCLCalculator(String resultName, Class<V> resultType) {
            super(resultName, resultType);
        }

        @Override
        public V calculate(MoleculeObject mo, boolean storeResult, boolean storeMol) {
            StereoMolecule mol = OCLMoleculeUtils.fetchMolecule(mo, storeMol);
            V result = calculate(mol);
            if (storeResult) {
                mo.putValue(resultName, result);
            }
            return result;
        }

        @Override
        public V calculate(MoleculeObject mo, boolean storeResult) {
            return calculate(mo, storeResult, false);
        }

        @Override
        public V calculate(MoleculeObject mo) {
            return calculate(mo, false, false);
        }

        public V calculate(StereoMolecule mol) {
            totalCount.incrementAndGet();
            try {
                return doCalculate(mol);
            } catch (Throwable t) {
                errorCount.incrementAndGet();
                LOG.log(Level.INFO, "OCL calculation " + resultName + " failed", t);
                return null;
            }
        }

        protected abstract V doCalculate(StereoMolecule mol);
    }
}
