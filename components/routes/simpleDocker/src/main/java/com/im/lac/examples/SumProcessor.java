package com.im.lac.examples;

import java.util.List;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 *
 * @author timbo
 */
public class SumProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        List<Number> values = exchange.getIn().getBody(List.class); 
        float sum = 0;
        int count = 0;
        for (Number value: values) {
            sum += value.floatValue();
            count++;
        }
        Statistics stats = new Statistics(values, sum, count);
        exchange.getIn().setBody(stats);
    }
    
}
