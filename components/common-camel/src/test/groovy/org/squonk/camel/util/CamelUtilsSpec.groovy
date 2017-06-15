/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.camel.util

import spock.lang.Specification

/**
 *
 * @author timbo
 */
class CamelUtilsSpec extends Specification {
    
    void "generateUrlUsingQueryParams"() {

        when:
        def headers = [:]
        def url = CamelUtils.generateUrlUsingHeadersAndQueryParams("http://localhost:1234/foo", ["query.apple":"green","query.banana":"yellow"], headers)
        
        then:
        url == "http://localhost:1234/foo?apple=green&banana=yellow"
        headers.size() == 0
        
    }
    
    void "generateUrlUsingHeaders"() {
        
        when:
        def headers = [:]
        def url = CamelUtils.generateUrlUsingHeadersAndQueryParams("http://localhost:1234/foo", ["header.apple":"green","header.banana":"yellow"], headers)
        
        then:
        url == "http://localhost:1234/foo"
        headers.size() == 2
    }
	
    
    void "generateUrlUsingHeadersAndQueryParams"() {
        
        when:
        def headers = [:]
        def url = CamelUtils.generateUrlUsingHeadersAndQueryParams("http://localhost:1234/foo", ["query.apple":"green","header.banana":"yellow"], headers)
        
        then:
        url == "http://localhost:1234/foo?apple=green"
        headers.size() == 1
        
    }
    
    
    void "generateUrlSkippedParams"() {

        
        when:
        def headers = [:]
        def url = CamelUtils.generateUrlUsingHeadersAndQueryParams("http://localhost:1234/foo",
            ["query.apple":"green","header.banana":"yellow","noprefix":"oranges","wrongprefix.lemon":"yellow"], 
            headers)
        
        then:
        url == "http://localhost:1234/foo?apple=green"
        headers.size() == 1
        
    }
    
    
    void "test null params"() {
        
        when:
        def headers = [:]
        def url = CamelUtils.generateUrlUsingHeadersAndQueryParams("http://localhost:1234/foo",
            null, 
            headers)
        
        then:
        url == "http://localhost:1234/foo"
        headers.size() == 0
        
    }
}

