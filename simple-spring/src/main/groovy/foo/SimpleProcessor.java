package foo;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 *
 * @author timbo
 */
public class SimpleProcessor implements Processor {
    
    private final String term;
    
    public SimpleProcessor() {
        this.term = "default";
    }
    
    public SimpleProcessor(String term) {
        this.term = term;
    }

    @Override
    public void process(Exchange exchng) throws Exception {
        System.out.println("Processing " + term);
    }
    
}
