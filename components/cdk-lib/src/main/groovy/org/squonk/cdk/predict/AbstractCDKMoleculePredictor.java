package org.squonk.cdk.predict;

import com.im.lac.types.MoleculeObject;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.squonk.cdk.io.CDKMoleculeIOUtils;
import org.squonk.property.MoleculeCalculator;
import org.squonk.property.Predictor;
import org.squonk.property.Property;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by timbo on 05/04/16.
 */
public abstract class AbstractCDKMoleculePredictor<V> extends Predictor<V,MoleculeObject,MoleculeCalculator<V>> {

    private static final Logger LOG = Logger.getLogger(AbstractCDKMoleculePredictor.class.getName());

    public AbstractCDKMoleculePredictor(String resultName, Property<V,MoleculeObject> propertyType) {
        super(resultName, propertyType);
    }


    public abstract class OCLCalculator<V> implements MoleculeCalculator<V> {

        protected final AtomicInteger totalCount = new AtomicInteger(0);
        protected final AtomicInteger errorCount = new AtomicInteger(0);

        @Override
        public V calculate(MoleculeObject mo, boolean storeResult, boolean storeMol) {
            IAtomContainer mol = null;
            try {
                mol = CDKMoleculeIOUtils.fetchMolecule(mo, storeMol);
            } catch (CDKException | CloneNotSupportedException e) {
                errorCount.incrementAndGet();
                LOG.log(Level.INFO, "CDK calculation " + getResultName() + " failed", e);
            }
            V result = calculate(mol);
            if (storeResult) {
                mo.putValue(getResultName(), result);
            }
            return result;
        }

        public V calculate(IAtomContainer mol) {
            totalCount.incrementAndGet();
            try {
                return doCalculate(mol);
            } catch (Throwable t) {
                errorCount.incrementAndGet();
                LOG.log(Level.INFO, "CDK calculation failed", t);
                return null;
            }
        }

        protected abstract V doCalculate(IAtomContainer mol);

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
