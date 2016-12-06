package org.squonk.execution.util

/**
 * Created by timbo on 04/12/16.
 */
class GroovyUtils {

    static String expandTemplate(def text, def binding) {
        def engine = new groovy.text.SimpleTemplateEngine()
        def template = engine.createTemplate(text).make(binding)
        return template.toString()
    }
}
