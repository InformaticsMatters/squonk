package org.squonk.execution.steps

import spock.lang.Specification

/**
 * Created by timbo on 01/08/17.
 */
class AbstractStepSpec extends Specification {

    void "generateStatusMessage no info"() {

        AbstractStep step = Spy()

        expect:
        def msg = step.generateStatusMessage(a,b,c)
        step.statusMessage == null // should not set
        msg == r

        where:
        a  | b  | c  | r
        -1 | -1 | -1 | AbstractStep.MSG_PROCESSING_COMPLETE
        10 | 11 | 12 | '10 processed, 11 results, 12 errors'
        10 | 11 | -1 | '10 processed, 11 results'
    }

    void "generateMetricsAndStatus default status"() {

        Properties props = new Properties()
        props.setProperty('__InputCount__', '10')
        props.setProperty('__OutputCount__', '11')
        props.setProperty('__ErrorCount__', '12')

        AbstractStep step = Spy()

        when:
        step.generateMetricsAndStatus(props, 123.4)

        then:
        step.numRecordsProcessed == 10
        step.numRecordsOutput == 11
        step.numErrors == 12
        step.statusMessage == '10 processed, 11 results, 12 errors'

    }


    void "generateMetricsAndStatus custom status"() {

        Properties props = new Properties()
        props.setProperty('__InputCount__', '10')
        props.setProperty('__OutputCount__', '11')
        props.setProperty('__ErrorCount__', '12')
        props.setProperty('__StatusMessage__', 'CustomMessage')

        AbstractStep step = Spy()

        when:
        step.generateMetricsAndStatus(props, 123.4)

        then:
        step.numRecordsProcessed == 10
        step.numRecordsOutput == 11
        step.numErrors == 12
        step.statusMessage == 'CustomMessage'

    }

}
