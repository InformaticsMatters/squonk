package org.squonk.cpsign

import org.squonk.data.Molecules
import org.squonk.dataset.Dataset
import org.squonk.types.CPSignTrainResult
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import java.util.stream.Collectors

/**
 * Created by timbo on 17/10/2016.
 */
@Stepwise
class CCPRegressionRunnerSpec extends Specification {

    @Shared CPSignTrainResult trainResult

    void cleanupSpec() {
        if (trainResult) {
            File dir = new File(CPSignConfig.workDir, trainResult.path)
            boolean b = dir.deleteDir()
        }
    }

    static int cvFolds = 3

    void "train"() {

        Dataset dataset = Molecules.datasetFromSDF("../../data/testfiles/glucocorticoid.sdf.gz")
        List items = dataset.stream.limit(100).collect {
            it.values['activity'] = new Double(it.values['target'])
            return it
        }
        println "${items.size()} molecules"
        CCPRegressionRunner runner = new CCPRegressionRunner(CPSignConfig.license, CPSignConfig.workDir)


        when:
        trainResult = runner.train(items, "activity", cvFolds, 0.7)
        println "cvFolds: $trainResult.cvFolds"
        println "Validity: $trainResult.validity"
        println "Efficiency: $trainResult.efficiency"
        println "RMSE: $trainResult.rmse"
        println "Path: $trainResult.path"

        then:
        trainResult != null
        trainResult.cvFolds == cvFolds

    }

    void predict() {
        Dataset dataset = Molecules.datasetFromSDF("../../data/testfiles/glucocorticoid.sdf.gz")
        CCPRegressionRunner runner = new CCPRegressionRunner(CPSignConfig.license, CPSignConfig.workDir)


        when:
        def AbstractCCPRunner.RegressionPredictor predictor = runner.createPredictor(trainResult.cvFolds, trainResult.path)
        def list = predictor.predict(dataset.stream.limit(10), "Activity", 0.7).collect(Collectors.toList())
        //list.forEach { println it }

        then:
        list.size() == 10
        list[0].values['Activity_Prediction'] != null
        list[0].values['Activity_Distance'] != null
        list[0].values['Activity_Signature'] != null
        list[0].values['Activity_AtomScores'] != null

    }

}
