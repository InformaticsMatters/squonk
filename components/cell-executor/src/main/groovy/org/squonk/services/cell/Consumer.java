package org.squonk.services.cell;

import org.apache.camel.Body;
import org.apache.camel.CamelContext;
import org.apache.camel.Consume;
import org.apache.camel.Exchange;

/** Example consumer. Not used.
 * Created by timbo on 27/01/16.
 */
public class Consumer {


    @Consume(uri = "direct:inbound")
    public void consume(CamelContext context, Exchange exch, @Body String body) {
        System.out.println("Consumer " + context + " " + exch + " " + body);
    }



}
