package org.squonk.options

import spock.lang.Specification

/**
 * Created by timbo on 27/04/17.
 */
class DatasetFieldTypeDescriptorSpec extends Specification {

    void "test filter"() {

        when:
        def d = new DatasetFieldTypeDescriptor('foo', [Number.class] as Class[])

        then:
        d.filter('bar', Float.class)
        d.filter('bar', Double.class)
        !d.filter('bar', String.class)

    }

    void "null input name becomes input"() {

        when:
        def d = new DatasetFieldTypeDescriptor(null, [Number.class] as Class[])

        then:
        d.inputName == "input"
    }

}
