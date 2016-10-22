package org.squonk.camel.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.squonk.cpsign.CCPClassifierRunner;
import org.squonk.cpsign.CCPRegressionRunner;
import org.squonk.cpsign.TrainResult;
import org.squonk.dataset.Dataset;
import org.squonk.types.MoleculeObject;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by timbo on 30/09/16.
 */
public class CPSignTrainProcessor implements Processor {

    public static final String HEADER_FIELD_NAME = "fieldName";
    public static final String HEADER_CV_FOLDS = "cvFolds";
    public static final String HEADER_CONFIDENCE = "confidence";
    public static final String HEADER_PREDICT_METHOD = "predictMethod";
    public static final String HEADER_PREDICT_LIBRARY = "predictLibrary";
    public static final String HEADER_PREDICT_TYPE = "predictType";
    public static final String HEADER_VALUE_1 = "value1";
    public static final String HEADER_VALUE_2 = "value2";
    public static final String HEADER_SIGNATURE_START_HEIGHT = "sigStart";
    public static final String HEADER_SIGNATURE_END_HEIGHT = "sigEnd";

    private static final Logger LOG = Logger.getLogger(CPSignTrainProcessor.class.getName());

    private File workDir = new File("/Users/timbo/tmp/cpsign");
    private File license = new File("/Users/timbo/dev/git/lac/data/licenses/cpsign0.3pro.license");

    @Override
    public void process(Exchange exch) throws Exception {

        Handler h = new Handler(exch.getIn());
        TrainResult result = h.train();
        if (result == null) {
            throw new IllegalStateException("Options cannot be interpreted");
        }

        //DatasetMetadata meta = handleMetadata(exch, dataset.getMetadata(), runner);
        exch.getIn().setBody(result);
    }

//    protected DatasetMetadata handleMetadata(Exchange exch, DatasetMetadata meta, SMARTCypRunner runner) throws IllegalAccessException, InstantiationException {
//        if (meta == null) {
//            meta = new DatasetMetadata(MoleculeObject.class);
//        }
//        String source = "SMARTCyp 2.4.2";
//        if (runner.isPerformGeneral()) {
//            meta.createField(SMARTCypRunner.FIELD_NAME_GEN, source, "General P450 metabolism prediction", AtomPropertySet.class);
//            //meta.appendFieldProperty(SMARTCypRunner.FIELD_NAME_GEN, DatasetMetadata.PROP_RANGE, runner.getGeneralRange());
//            meta.appendFieldProperty(SMARTCypRunner.FIELD_NAME_GEN, DatasetMetadata.PROP_SCALE, Scales.SMARTCyp);
//        }
//        if (runner.isPerform2D6()) {
//            meta.createField(SMARTCypRunner.FIELD_NAME_2D6, source, "Cytochrome 2D6 metabolism prediction", AtomPropertySet.class);
//            //meta.appendFieldProperty(SMARTCypRunner.FIELD_NAME_2D6, DatasetMetadata.PROP_RANGE, runner.get2D6Range());
//            meta.appendFieldProperty(SMARTCypRunner.FIELD_NAME_2D6, DatasetMetadata.PROP_SCALE, Scales.SMARTCyp);
//        }
//        if (runner.isPerform2C9()) {
//            meta.createField(SMARTCypRunner.FIELD_NAME_2C9, source, "Cytochrome 2C9 metabolism prediction", AtomPropertySet.class);
//            //meta.appendFieldProperty(SMARTCypRunner.FIELD_NAME_2C9, DatasetMetadata.PROP_RANGE, runner.get2C9Range());
//            meta.appendFieldProperty(SMARTCypRunner.FIELD_NAME_2C9, DatasetMetadata.PROP_SCALE, Scales.SMARTCyp);
//        }
//
//        return meta;
//    }


    class Handler {

        Dataset<MoleculeObject> dataset;
        TrainResult.Method method;
        TrainResult.Library library;
        TrainResult.Type type;
        String fieldName;
        Integer cvFolds;
        Double confidence;
        Integer startHeight;
        Integer endHeight;
        String value1 = null;
        String value2 = null;


        Handler(Message msg) {
            dataset = msg.getBody(Dataset.class);
            if (dataset == null || dataset.getType() != MoleculeObject.class) {
                throw new IllegalStateException("Input must be a Dataset of MoleculeObjects");
            }

            fieldName = msg.getHeader(HEADER_FIELD_NAME, String.class);
            if (fieldName == null || fieldName.isEmpty()) {
                throw new IllegalStateException("Field name containing data to be trained not specified");
            }

            String methodStr = msg.getHeader(HEADER_PREDICT_METHOD, String.class);
            method = methodStr == null ? TrainResult.Method.CCP : TrainResult.Method.valueOf(methodStr);

            String libraryStr = msg.getHeader(HEADER_PREDICT_LIBRARY, String.class);
            library = libraryStr == null ? TrainResult.Library.LibSVM : TrainResult.Library.valueOf(libraryStr);

            String typeStr = msg.getHeader(HEADER_PREDICT_TYPE, String.class);
            if (typeStr == null || typeStr.isEmpty()) {
                throw new IllegalStateException("Prediction type must specified as Regression or Classification");
            }

            type = TrainResult.Type.valueOf(typeStr);
            if (type == TrainResult.Type.Classification) {
                value1 = msg.getHeader(HEADER_VALUE_1, String.class);
                if (value1 == null || value1.isEmpty()) {
                    throw new IllegalStateException("Value 1 must be specified for regression");
                }
                value2 = msg.getHeader(HEADER_VALUE_2, String.class);
                if (value2 == null || value2.isEmpty()) {
                    throw new IllegalStateException("Value 2 must be specified for regression");
                }
            }

            cvFolds = msg.getHeader(HEADER_CV_FOLDS, Integer.class);
            if (cvFolds == null) {
                cvFolds = 5;
            }
            confidence = msg.getHeader(HEADER_CONFIDENCE, Double.class);
            if (confidence == null) {
                confidence = 0.7d;
            }
            startHeight = msg.getHeader(HEADER_SIGNATURE_START_HEIGHT, Integer.class);
            if (startHeight == null) {
                startHeight = 1;
            }
            endHeight = msg.getHeader(HEADER_SIGNATURE_END_HEIGHT, Integer.class);
            if (endHeight == null) {
                endHeight = 3;
            }
        }

        TrainResult train() throws Exception {
            TrainResult result;
            List<MoleculeObject> mols = dataset.getItems();
            switch (method) {
                case CCP:
                    if (type == TrainResult.Type.Regression) {
                        CCPRegressionRunner runner = new CCPRegressionRunner(license, workDir, library, startHeight, endHeight);
                        result = runner.train(mols, fieldName, cvFolds, confidence);
                    } else {
                        CCPClassifierRunner runner = new CCPClassifierRunner(license, workDir, library, startHeight, endHeight);
                        result = runner.train(mols, fieldName, value1, value2, cvFolds, confidence);
                    }
                    break;
                default:
                    throw new UnsupportedOperationException("Method " + method + " not yet supported");

            }
            return result;
        }


    }

}
