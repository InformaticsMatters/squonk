package com.im.lac.camel.chemaxon.processor;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 *
 * @author timbo
 */
public class HeaderFromFileProcessor implements Processor {

    private final File file;
    private final String header;
    private static final Logger LOG = Logger.getLogger(HeaderFromFileProcessor.class.getName());

    public HeaderFromFileProcessor(String header, File file) {
        assert header != null;
        assert file != null;
        this.header = header;
        this.file = file;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        if (file.exists()) {
            String txt = exchange.getContext().getTypeConverter().convertTo(String.class, exchange, file);
            exchange.getIn().setHeader(header, txt);
        } else {
            LOG.log(Level.WARNING, "File {0} is defined but does not exist", file.getName());
        }
    }

}
