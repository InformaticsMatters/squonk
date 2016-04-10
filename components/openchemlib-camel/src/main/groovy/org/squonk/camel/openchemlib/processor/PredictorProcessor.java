package org.squonk.camel.openchemlib.processor;

import com.im.lac.types.MoleculeObject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.squonk.camel.CamelCommonConstants;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.openchemlib.predict.AbstractOCLPredictor;
import org.squonk.property.Calculator;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by timbo on 05/04/16.
 */
public class PredictorProcessor implements Processor {

    private static final Logger LOG = Logger.getLogger(PredictorProcessor.class.getName());

    private final List<AbstractOCLPredictor> predictors = new ArrayList<>();


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

        for (AbstractOCLPredictor predictor : predictors) {
            Calculator calc = predictor.getCalculator();
            mols = calculateMultiple(mols, calc);
            // TODO - handle the stats from the calculator, but bear in mind that the calculations won't happen until the stream is processed.
        }
        handleMetadata(exch, dataset.getMetadata());
        exch.getIn().setBody(new MoleculeObjectDataset(mols));
    }

    protected Stream<MoleculeObject> calculateMultiple(Stream<MoleculeObject> input, Calculator calc) {

        input = input.peek((mo) -> {
            try {
                calc.calculate(mo, true, true);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Failed to evaluate molecule", ex);
            }
        });
        return input;
    }

    protected void handleMetadata(Exchange exch, DatasetMetadata meta) throws IllegalAccessException, InstantiationException {
        if (meta == null) {
            meta = new DatasetMetadata(MoleculeObject.class);
        }
        for (AbstractOCLPredictor predictor : predictors) {
            String name = predictor.getResultName();
            Class type = predictor.getPropertyType().getValueClass();
            meta.getValueClassMappings().put(name, type);
        }
        exch.getIn().setHeader(CamelCommonConstants.HEADER_METADATA, meta);
    }

}
