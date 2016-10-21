package org.squonk.cpsign

import org.squonk.data.Molecules
import org.squonk.dataset.Dataset
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import java.util.stream.Collectors

/**
 * Created by timbo on 17/10/2016.
 */
@Stepwise
class CCPClassifierRunnerSpec extends Specification {

    @Shared TrainResult trainResult

    static int cvFolds = 3

    void "train"() {

        Dataset dataset = Molecules.datasetFromSDF("../../data/testfiles/bursi_classification.sdf.gz")
        List items = dataset.items
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
        Dataset dataset = Molecules.nci100Dataset()
        CCPClassifierRunner runner = new CCPClassifierRunner(CPSignConfig.license, CPSignConfig.workDir)


        when:
        def AbstractCCPRunner.ClassificationPredictor predictor = runner.createPredictor(trainResult.cvFolds, trainResult.path)
        def list = predictor.predict(dataset.getStream(), "Mutagen").collect(Collectors.toList())


        then:
        list.size() == 100
        list[0].values['Mutagen_PVal_F'] != null
        list[0].values['Mutagen_PVal_T'] != null
        list[0].values['Mutagen_PVal_T'] != null
        list[0].values['Mutagen_AtomScores'] != null

    }

}
