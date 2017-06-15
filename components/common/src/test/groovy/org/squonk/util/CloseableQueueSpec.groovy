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

package org.squonk.util

import org.squonk.util.CloseableQueue
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class CloseableQueueSpec extends Specification {
    
    def 'simple iterator'() {

        given:
        CloseableQueue q = new CloseableQueue(20)
        
        when:
        (1..10).each {
            q.add(it)
        }
        q.close()

        then:
        //q.hasNext() == true
        //q.next() == 1
        q.collect().size() == 10
    }
    
    
    def 'slow producer'() {

        given:
        CloseableQueue q = new CloseableQueue(5)
        def output = []
        
        when:
        Thread t = new Thread(new Runnable() {
                void run() {
                    while (q.hasNext()) {
                        output << q.next()
                    }
                }
            })
        t.start()

        (1..100).each {
            q.add(it)
            sleep(5)
        }
        q.close()
        t.join()

        then:
        output.size() == 100
    }
    
    def 'slow consumer'() {

        given:
        CloseableQueue q = new CloseableQueue(5)
        def output = []
        
        when:
        Thread t = new Thread(new Runnable() {
                void run() {
                    while (q.hasNext()) {
                        output << q.next()
                        sleep(5)
                    }
                }
            })
        t.start()

        (1..100).each {
            q.add(it)
        }
        q.close()
        t.join()

        then:
        output.size() == 100
    }	
}

