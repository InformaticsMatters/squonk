package org.squonk.execution.runners

import spock.lang.Specification

/**
 * Created by timbo on 02/08/17.
 */
class NextflowRunnerSpec extends Specification {

    void "clean workdir"() {

        setup:
        NextflowRunner runner = new NextflowRunner()
        runner.init()

        when:
        runner.cleanup()

        then:
        !runner.getHostWorkDir().exists()
    }

    void "simple execute"() {

        setup:
        NextflowRunner runner = new NextflowRunner()
        runner.init()

        def nextflowFile = '''
inp = file("$baseDir/nextflow.nf")
outp = file("$baseDir/result.nf")

process sayHello {

    publishDir baseDir

    input:
    file inp

    output:
    file 'result.nf'

    """
    printf 'Hello world! \\n\'
    cp $inp result.nf
    """
}'''

        when:
        runner.writeInput("nextflow.nf", nextflowFile)
        def args = [runner.getHostWorkDir().path + "/nextflow.nf"]
        runner.execute(args, [:])

        then:
        new File(runner.getHostWorkDir(), 'result.nf').exists()
        runner.readOutput("result.nf").text == nextflowFile

        cleanup:
        runner.cleanup()
    }

}
