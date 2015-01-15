package com.im.lac.camel.chemaxon.processor;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 *
 * @author timbo
 */
public class HeaderPropertySetterProcessor implements Processor {
    
    private final File propertiesFile;
    private static final Logger LOG = Logger.getLogger(HeaderPropertySetterProcessor.class.getName());
    
    public HeaderPropertySetterProcessor(File propertiesFile) {
        this.propertiesFile = propertiesFile;
    }
    
    @Override
    public void process(Exchange exchange) throws Exception {
        if (propertiesFile != null) {
            if (propertiesFile.exists()) {
                Properties props = new Properties();
                props.load(new FileInputStream(propertiesFile));
                for (Map.Entry e : props.entrySet()) {
                    String key = (String) e.getKey();
                    LOG.log(Level.INFO, "Setting header {0} to {1}", new Object[]{key, e.getValue()});
                    exchange.getIn().setHeader(key, e.getValue());
                }
            } else {
                LOG.log(Level.WARNING, "Properties file {0} is defined but does not exist", propertiesFile.getName());
            }
        }
    }
    
}
