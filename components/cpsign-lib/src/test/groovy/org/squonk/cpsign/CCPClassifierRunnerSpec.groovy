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
class CCPClassifierRunnerSpec extends Specification {

    @Shared CPSignTrainResult trainResult

    void cleanupSpec() {
        if (trainResult) {
            File dir = new File(CPSignConfig.workDir, trainResult.path)
            boolean b = dir.deleteDir()
        }
    }

    static int cvFolds = 3

    void "train"() {

        Dataset dataset = Molecules.datasetFromSDF("../../data/testfiles/bursi_classification.sdf.gz")
        List items = dataset.items.take(100)
        println "${items.size()} molecules"
        CCPClassifierRunner runner = new CCPClassifierRunner(CPSignConfig.license, CPSignConfig.workDir)


        when:
        trainResult = runner.train(items, "Ames test categorisation", "mutagen", "nonmutagen", cvFolds, 0.7)
        println "cvFolds: $trainResult.cvFolds"
        println "Validity: $trainResult.validity"
        println "Efficiency: $trainResult.efficiency"
        println "Path: $trainResult.path"

        then:
        trainResult != null
        trainResult.cvFolds == cvFolds

    }

    void predict() {
        //Dataset dataset = Molecules.nci100Dataset()

        Dataset dataset = Molecules.datasetFromSDF("../../data/testfiles/bursi_classification.sdf.gz")

        CCPClassifierRunner runner = new CCPClassifierRunner(CPSignConfig.license, CPSignConfig.workDir)


        when:
        def AbstractCCPRunner.ClassificationPredictor predictor = runner.createPredictor(trainResult.cvFolds, trainResult.path)
        def list = predictor.predict(dataset.getStream().limit(10), "Mutagen").collect(Collectors.toList())
        //list.forEach { println it }


        then:
        list.size() == 10
        list[0].values['Mutagen_PVal_F'] != null
        list[0].values['Mutagen_PVal_T'] != null
        list[0].values['Mutagen_AtomScores'] != null
        list[0].values['Mutagen_Signature'] != null

    }

}
