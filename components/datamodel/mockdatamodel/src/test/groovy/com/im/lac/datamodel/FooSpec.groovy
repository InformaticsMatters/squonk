/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.im.lac.datamodel

import spock.lang.Specification

/**
 *
 * @author timbo
 */
class FooSpec extends Specification {
    
    def "simple test"() {
        def foo = new Foo()
        
        expect:
        'Hello world!' == foo.sayHello() 
    }
	
}

