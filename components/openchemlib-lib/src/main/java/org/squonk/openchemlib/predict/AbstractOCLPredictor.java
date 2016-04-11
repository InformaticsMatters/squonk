package org.squonk.openchemlib.predict;

import com.actelion.research.chem.StereoMolecule;
import com.im.lac.types.MoleculeObject;
import org.squonk.openchemlib.molecule.OCLMoleculeUtils;
import org.squonk.property.Calculator;
import org.squonk.property.MoleculeCalculator;
import org.squonk.property.Predictor;
import org.squonk.property.Property;

import java.util.concurrent.atomic.AtomicInteger;
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


    public abstract class OCLCalculator<V> implements MoleculeCalculator<V> {

        protected final AtomicInteger totalCount = new AtomicInteger(0);
        protected final AtomicInteger errorCount = new AtomicInteger(0);

        @Override
        public V calculate(MoleculeObject mo, boolean storeResult, boolean storeMol) {
            StereoMolecule mol = OCLMoleculeUtils.fetchMolecule(mo, storeMol);
            V result = calculate(mol);
            if (storeResult) {
                mo.putValue(getResultName(), result);
            }
            return result;
        }

        @Override
        public V calculate(MoleculeObject mo, boolean storeResult) {
            return calculate(mo, storeResult, false);
        }

        public V calculate(StereoMolecule mol) {
            totalCount.incrementAndGet();
            try {
                return doCalculate(mol);
            } catch (Throwable t) {
                errorCount.incrementAndGet();
                LOG.log(Level.INFO, "OCL calculation " + getResultName() + " failed", t);
                return null;
            }
        }

        protected abstract V doCalculate(StereoMolecule mol);

        @Override
        public int getTotalCount() {
            return totalCount.get();
        }

        @Override
        public int getErrorCount() {
            return errorCount.get();
        }
    }
}
