package org.squonk.camel.openchemlib.processor;

import com.actelion.research.chem.Molecule;
import org.squonk.types.MoleculeObject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.squonk.camel.CamelCommonConstants;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.openchemlib.predict.AbstractOCLPredictor;
import org.squonk.property.MoleculeCalculator;
import org.squonk.util.ExecutionStats;
import org.squonk.util.StatsRecorder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by timbo on 05/04/16.
 */
public class PredictorProcessor implements Processor {

    private static final Logger LOG = Logger.getLogger(PredictorProcessor.class.getName());

    private final List<AbstractOCLPredictor<?>> predictors = new ArrayList<>();


    public PredictorProcessor calculate(AbstractOCLPredictor predictor) {
        predictors.add(predictor);
        return this;
    }


    @Override
    public void process(Exchange exch) throws Exception {
        Dataset<MoleculeObject> dataset = exch.getIn().getBody(Dataset.class);
        if (dataset == null || dataset.getType() != MoleculeObject.class) {
            throw new IllegalStateException("Input must be a Dataset of MoleculeObjects");
        }
        Stream<MoleculeObject> mols = dataset.getStream();

        for (AbstractOCLPredictor<?> predictor : predictors) {
            MoleculeCalculator<?>[] calcs = predictor.getCalculators();
            for (MoleculeCalculator<?> calc : calcs) {
                mols = calculateMultiple(mols, calc);
                // TODO - handle the stats from the calculator, but bear in mind that the calculations won't happen until the stream is processed.
            }
        }
        StatsRecorder recorder = exch.getIn().getHeader(StatsRecorder.HEADER_STATS_RECORDER, StatsRecorder.class);
        if (recorder != null) {
            mols = mols.onClose(() -> {

                List<Map<String,Integer>> stats = new ArrayList<>();
                for (AbstractOCLPredictor predictor : predictors) {
                    stats.add(predictor.getExecutionStats());
                }
                recorder.recordStats(stats);
            });
        }
        DatasetMetadata meta = handleMetadata(exch, dataset.getMetadata());
        exch.getIn().setBody(new MoleculeObjectDataset(mols, meta));
    }

    protected DatasetMetadata handleMetadata(Exchange exch, DatasetMetadata meta) throws IllegalAccessException, InstantiationException {
        if (meta == null) {
            meta = new DatasetMetadata(MoleculeObject.class);
        }
        String source = "OpenChemLib " + Molecule.class.getPackage().getImplementationVersion();
        for (AbstractOCLPredictor<?> predictor : predictors) {
            int i = 0;
            for (MoleculeCalculator<?> calc : predictor.getCalculators()) {
                String desc = predictor.getPropertyTypes()[i].getDescription();
                meta.createField(calc.getResultName(), source, "Molecular property calculation: " + desc, calc.getResultType());
                i++;
            }
        }
        return meta;
    }

    protected Stream<MoleculeObject> calculateMultiple(Stream<MoleculeObject> input, MoleculeCalculator<?> calc) {

        input = input.peek((mo) -> {
            try {
                calc.calculate(mo, true, true);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Failed to evaluate molecule", ex);
            }
        });
        return input;
    }
}
