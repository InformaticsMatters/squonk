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

